package absoluta.connection;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.javatuples.Pair;

import protocol.dsc.DscError;
import protocol.dsc.Endpoint;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.Messenger;
import protocol.dsc.NewValue;
import protocol.dsc.Message.Response;

public class MessageHandler {
   private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
   private static final int RETRY_NUMBER = 4;
   private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
   private static final long START_TIMEOUT= TimeUnit.SECONDS.toMillis(40);
   private static final long RETRY_TIMEOUT = TimeUnit.SECONDS.toMillis(2);
   private final Messenger messenger;
   private final ScheduledExecutorService executor;
   private final ConnectionHandler.ErrorListener errorListener;
   private final Queue<EnqueuedMessage<?>> enqueuedMessages = new PriorityQueue<>(256, new MessageComparator());
   private final Queue<Runnable> idleTimeTasks = new ArrayDeque<>();
   private ScheduledFuture<?> retryFuture;
   private EnqueuedMessage<?> lastMessage;
   private boolean started;
   private boolean stopped;
   private int messageCount;

   MessageHandler(Endpoint endpoint, ConnectionHandler.ErrorListener errorListener) {
      this.messenger = Objects.requireNonNull(endpoint.getMessenger());
      this.executor = Objects.requireNonNull(endpoint.getExecutor());
      this.errorListener = Objects.requireNonNull(errorListener);
      this.messenger.addMessageListener(new MHMessageListener());
      this.executor.schedule(() -> {
         if (!MessageHandler.this.started && !MessageHandler.this.stopped) {
            logger.warning("Timeout before starting to send messages");
            MessageHandler.this.errorListener.fatalError();
         }
      }, START_TIMEOUT, TIME_UNIT);
   }

   public <P> void sendCommand(Message<P, ?> message, P param) {
      this.send(message, param, MessageType.COMMAND, null);
   }

   public <P> void sendHighPriorityReading(Message<P, ?> message, P param) {
      this.send(message, param, MessageType.HIGH_PRIORITY_READING, null);
   }

   /**
    * Sends a high priority reading that must not abort the connection if it keeps timing out.
    * When the retries are exhausted, {@code onAbandon} is run (e.g. to enqueue a fallback) and
    * message processing continues instead of raising a fatal error.
    */
   public <P> void sendHighPriorityReading(Message<P, ?> message, P param, Runnable onAbandon) {
      this.send(message, param, MessageType.HIGH_PRIORITY_READING, onAbandon);
   }

   public <P> void sendMidPriorityReading(Message<P, ?> message, P param) {
      this.send(message, param, MessageType.MID_PRIORITY_READING, null);
   }

   public <P> void sendMidPriorityReading(Message<P, ?> message, P param, Runnable onAbandon) {
      this.send(message, param, MessageType.MID_PRIORITY_READING, onAbandon);
   }

   public <P> void sendLowPriorityReading(Message<P, ?> message, P param) {
      this.send(message, param, MessageType.LOW_PRIORITY_READING, null);
   }

   public void scheduleIdleTimeTask(final Runnable task) {
      this.executor.submit(() -> {
         MessageHandler.this.idleTimeTasks.add(task);
         if (MessageHandler.this.enqueuedMessages.isEmpty()) {
               MessageHandler.this.executeIdleTimeTasks();
         }
      });
   }

   void start() {
      logger.fine("Starting message handler");
      this.executor.submit(() -> {
         if (!MessageHandler.this.started) {
               MessageHandler.this.started = true;
               MessageHandler.this.sendNext();
         }
      });
   }

   void stop() {
      logger.fine("Stopping message handler");
      this.executor.submit(() -> {
         MessageHandler.this.stopped = true;
         if (MessageHandler.this.retryFuture != null) {
               MessageHandler.this.retryFuture.cancel(false);
         }
      });
   }

   private <P> void send(final Message<P, ?> message, final P param, final MessageType type, final Runnable onAbandon) {
      this.executor.submit(() -> MessageHandler.this.enqueue(new EnqueuedMessage<>(message, param, type, onAbandon)));
   }

   private void enqueue(EnqueuedMessage<?> msg) {
      if (this.started && this.enqueuedMessages.isEmpty() && (this.lastMessage == null || this.lastMessage.responseReceived)) {
         logger.finest("Sending immediately " + msg);
         this.sendMessage(msg);
      } else if (msg.type != MessageType.COMMAND && this.enqueuedMessages.contains(msg)) {
         logger.finest("Discarding " + msg);
      } else {
         logger.finest("Enqueuing " + msg);
         this.enqueuedMessages.add(msg);
      }
   }

   private void sendNext() {
      EnqueuedMessage<?> msg = this.enqueuedMessages.poll();
      if (msg != null) {
         logger.finest("Sending next enqueued message " + msg);
         this.sendMessage(msg);
      } else {
         this.executeIdleTimeTasks();
      }
   }

   private void executeIdleTimeTasks() {
      if (this.started && !this.stopped) {
         Runnable task;
         while ((task = this.idleTimeTasks.poll()) != null) {
               try {
                  logger.fine("Running idle time task: " + task);
                  task.run();
               } catch (RuntimeException ex) {
                  logger.severe("Error running an idle time task: " + ex);
               }
         }
      }
   }

   private void manageError(Integer responseCode) {
      assert this.started && this.lastMessage != null;
      if (this.lastMessage.type == MessageType.COMMAND && responseCode != null) {
         logger.finest("Command " + this.lastMessage + " discarded");
         this.sendNext();
      } else if (this.lastMessage.attemptNum < RETRY_NUMBER) {
         logger.finer("Retry N° " + this.lastMessage.attemptNum + ": " + this.lastMessage + " ...");
         this.sendMessage(this.lastMessage);
      } else if (this.lastMessage.onAbandon != null) {
         // The caller provided a fallback: never fatal — run it and keep going.
         logger.warning("Too many attempts for " + this.lastMessage + ", running fallback");
         Runnable onAbandon = this.lastMessage.onAbandon;
         this.lastMessage = null;
         onAbandon.run();
         this.sendNext();
      } else if (this.lastMessage.type == MessageType.MID_PRIORITY_READING
               || this.lastMessage.type == MessageType.LOW_PRIORITY_READING) {
         // Transient panel unavailability (e.g. busy arming) — skip and continue
         logger.warning("Too many attempts for " + this.lastMessage + ", skipping");
         this.sendNext();
      } else {
         logger.warning("Too many attempts for " + this.lastMessage);
         this.errorListener.fatalError();
      }
   }

   private void sendMessage(final EnqueuedMessage<?> msg) {
      assert this.started;
      if (!this.stopped) {
         if (this.retryFuture != null) {
               this.retryFuture.cancel(false);
         }
         this.lastMessage = msg;
         this.retryFuture = this.executor.schedule(() -> {
               assert MessageHandler.this.lastMessage == msg;
               if (!msg.responseReceived) {
                  logger.warning("Response timeout for " + msg);
                  MessageHandler.this.manageError(null);
               }
         }, RETRY_TIMEOUT, TIME_UNIT);
         this.lastMessage.send();
      }
   }

   private boolean checkResponse(Response response) {
      if (this.lastMessage == null || this.lastMessage.message != response.getMessage()) {
         return false;
      }

      if (this.lastMessage.message == Message.ZONE_STATUSES && response instanceof NewValue
            && !zoneStatusResponseCoversRequest(
                  this.lastMessage.param, response.getParam(Message.ZONE_STATUSES))) {
         return false;
      }

      this.lastMessage.responseReceived = true;
      return true;
   }

   static boolean zoneStatusResponseCoversRequest(Object requestedParam, Pair<Integer, Integer> responseRange) {
      if (!(requestedParam instanceof Pair) || responseRange == null) {
         return false;
      }

      Pair<?, ?> requestedRange = (Pair<?, ?>)requestedParam;
      if (!(requestedRange.getValue0() instanceof Integer)
            || !(requestedRange.getValue1() instanceof Integer)) {
         return false;
      }

      int requestedStart = (Integer)requestedRange.getValue0();
      int requestedCount = (Integer)requestedRange.getValue1();
      int responseStart = responseRange.getValue0();
      int responseCount = responseRange.getValue1();
      return responseStart <= requestedStart
            && requestedStart + requestedCount <= responseStart + responseCount;
   }

   private class EnqueuedMessage<P> {
      private final Message<P, ?> message;
      private final P param;
      private final MessageType type;
      private final Runnable onAbandon;
      private final int n;
      private boolean responseReceived;
      private int attemptNum;

      EnqueuedMessage(Message<P, ?> message, P param, MessageType type, Runnable onAbandon) {
         this.message = Objects.requireNonNull(message);
         this.param = param;
         this.type = Objects.requireNonNull(type);
         this.onAbandon = onAbandon;
         this.n = MessageHandler.this.messageCount++;
      }

      void send() {
         ++this.attemptNum;
         MessageHandler.this.messenger.send(this.message, this.param);
      }

      @Override
      public String toString() {
         return String.format("%s(%s) [type: %s, resp: %b, att: %d, n: %d]", this.message, this.param, this.type, this.responseReceived, this.attemptNum, this.n);
      }

      @Override
      public int hashCode() {
         int hash = 7;
         hash = 97 * hash + Objects.hashCode(this.message);
         hash = 97 * hash + Objects.hashCode(this.param);
         hash = 97 * hash + Objects.hashCode(this.type);
         return hash;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == null) return false;
         if (this.getClass() != obj.getClass()) return false;
         EnqueuedMessage<?> other = (EnqueuedMessage<?>) obj;
         return Objects.equals(this.message, other.message)
                  && Objects.equals(this.param, other.param)
                  && Objects.equals(this.type, other.type);
      }
   }

   private class MHMessageListener implements MessageListener {
      @Override
      public void newValue(NewValue value) {
         if (MessageHandler.this.checkResponse(value)) {
               logger.fine("Response received for " + MessageHandler.this.lastMessage);
               MessageHandler.this.sendNext();
         }
      }

      @Override
      public void error(DscError error) {
         if (MessageHandler.this.checkResponse(error)) {
               logger.fine("Error received for " + MessageHandler.this.lastMessage + ": " + error.getDescription());
               MessageHandler.this.manageError(error.getResponseCode());
         }
      }
   }

   private static class MessageComparator implements Comparator<EnqueuedMessage<?>> {
      @Override
      public int compare(EnqueuedMessage<?> m1, EnqueuedMessage<?> m2) {
         int typeCompare = m1.type.compareTo(m2.type);
         return typeCompare != 0 ? typeCompare : Integer.compare(m1.n, m2.n);
      }
   }

   private enum MessageType {
      COMMAND,
      HIGH_PRIORITY_READING,
      MID_PRIORITY_READING,
      LOW_PRIORITY_READING
   }
}