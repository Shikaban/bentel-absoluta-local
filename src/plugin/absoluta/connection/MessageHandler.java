package plugin.absoluta.connection;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import protocol.dsc.DscError;
import protocol.dsc.Endpoint;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.Messenger;
import protocol.dsc.NewValue;
import protocol.dsc.Message.Response;

public class MessageHandler {
   private static final int RETRY_NUMBER = 3;
   private static final TimeUnit TIME_UNIT;
   private static final long START_TIMEOUT;
   private static final long RETRY_TIMEOUT;
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
   private static final boolean VERBOSE_DEBUG = false;

   static {
      TIME_UNIT = TimeUnit.MILLISECONDS;
      START_TIMEOUT = TimeUnit.SECONDS.toMillis(40L);
      RETRY_TIMEOUT = TimeUnit.SECONDS.toMillis(5L);
   }

   MessageHandler(Endpoint endpoint, ConnectionHandler.ErrorListener errorListener) {
      this.messenger = Objects.requireNonNull(endpoint.getMessenger());
      this.executor = Objects.requireNonNull(endpoint.getExecutor());
      this.errorListener = Objects.requireNonNull(errorListener);
      this.messenger.addMessageListener(new MHMessageListener());
      this.executor.schedule(() -> {
         if (!MessageHandler.this.started && !MessageHandler.this.stopped) {
            System.out.println("WARN: timeout before starting to send messages");
            MessageHandler.this.errorListener.fatalError();
         }
      }, START_TIMEOUT, TIME_UNIT);
   }

   public <P> void sendCommand(Message<P, ?> message, P param) {
      this.send(message, param, MessageType.COMMAND);
   }

   public <P> void sendHighPriorityReading(Message<P, ?> message, P param) {
      this.send(message, param, MessageType.HIGH_PRIORITY_READING);
   }

   public <P> void sendMidPriorityReading(Message<P, ?> message, P param) {
      this.send(message, param, MessageType.MID_PRIORITY_READING);
   }

   public <P> void sendLowPriorityReading(Message<P, ?> message, P param) {
      this.send(message, param, MessageType.LOW_PRIORITY_READING);
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
      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: starting message handler");
      }
      this.executor.submit(() -> {
         if (!MessageHandler.this.started) {
               MessageHandler.this.started = true;
               MessageHandler.this.sendNext();
         }
      });
   }

   void stop() {
      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: stopping message handler");
      }
      this.executor.submit(() -> {
         MessageHandler.this.stopped = true;
         if (MessageHandler.this.retryFuture != null) {
               MessageHandler.this.retryFuture.cancel(false);
         }
      });
   }

   private <P> void send(final Message<P, ?> message, final P param, final MessageType type) {
      this.executor.submit(() -> MessageHandler.this.enqueue(new EnqueuedMessage<>(message, param, type)));
   }

   private void enqueue(EnqueuedMessage<?> msg) {
      if (this.started && this.enqueuedMessages.isEmpty() && (this.lastMessage == null || this.lastMessage.responseReceived)) {
         if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: sending immediately " + msg);
         }
         this.sendMessage(msg);
      } else if (msg.type != MessageType.COMMAND && this.enqueuedMessages.contains(msg)) {
         if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: discarding " + msg);
         }
      } else {
         if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: enqueuing " + msg);
         }
         this.enqueuedMessages.add(msg);
      }
   }

   private void sendNext() {
      EnqueuedMessage<?> msg = this.enqueuedMessages.poll();
      if (msg != null) {
         if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: sending next enqueued message " + msg);
         }
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
                  if (VERBOSE_DEBUG) {
                     System.out.println("DEBUG: running idle time task: " + task);
                  }
                  task.run();
               } catch (RuntimeException ex) {
                  System.out.println("ERROR: error running an idle time task: " + ex);
               }
         }
      }
   }

   private void manageError(Integer responseCode) {
      assert this.started && this.lastMessage != null;
      if (this.lastMessage.type == MessageType.COMMAND && responseCode != null) {
         if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: command " + this.lastMessage + " discarded");
         }
         this.sendNext();
      } else if (this.lastMessage.attemptNum < RETRY_NUMBER) {
         if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: retrying " + this.lastMessage + " ...");
         }
         this.sendMessage(this.lastMessage);
      } else {
         System.out.println("WARN: too many attempts for " + this.lastMessage);
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
                  System.out.println("WARN: response timeout for " + msg);
                  MessageHandler.this.manageError(null);
               }
         }, RETRY_TIMEOUT, TIME_UNIT);
         this.lastMessage.send();
      }
   }

   private boolean checkResponse(Response response) {
      if (this.lastMessage != null && this.lastMessage.message == response.getMessage()) {
         this.lastMessage.responseReceived = true;
         return true;
      } else {
         return false;
      }
   }

   private class EnqueuedMessage<P> {
      private final Message<P, ?> message;
      private final P param;
      private final MessageType type;
      private final int n;
      private boolean responseReceived;
      private int attemptNum;

      EnqueuedMessage(Message<P, ?> message, P param, MessageType type) {
         this.message = Objects.requireNonNull(message);
         this.param = param;
         this.type = Objects.requireNonNull(type);
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
               if (VERBOSE_DEBUG) {
                  System.out.println("DEBUG: response received for " + MessageHandler.this.lastMessage);
               }
               MessageHandler.this.sendNext();
         }
      }

      @Override
      public void error(DscError error) {
         if (MessageHandler.this.checkResponse(error)) {
               if (VERBOSE_DEBUG) {
                  System.out.println("DEBUG: error received for " + MessageHandler.this.lastMessage + ": " + error.getDescription());
               }
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
