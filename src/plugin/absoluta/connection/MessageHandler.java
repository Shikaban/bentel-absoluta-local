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
   private final Queue<MessageHandler.EnqueuedMessage<?>> enqueuedMessages = new PriorityQueue(256, new MessageHandler.MessageComparator());
   private final Queue<Runnable> idleTimeTasks = new ArrayDeque();
   private ScheduledFuture<?> retryFuture;
   private MessageHandler.EnqueuedMessage<?> lastMessage;
   private boolean started;
   private boolean stopped;
   private int messageCount;
   private static final boolean VERBOSE_DEBUG = false;

   MessageHandler(Endpoint var1, ConnectionHandler.ErrorListener var2) {
      this.messenger = (Messenger)Objects.requireNonNull(var1.getMessenger());
      this.executor = (ScheduledExecutorService)Objects.requireNonNull(var1.getExecutor());
      this.errorListener = (ConnectionHandler.ErrorListener)Objects.requireNonNull(var2);
      this.messenger.addMessageListener(new MessageHandler.MHMessageListener());
      this.executor.schedule(new Runnable() {
         public void run() {
            if (!MessageHandler.this.started && !MessageHandler.this.stopped) {
               System.out.println("WARN: timeout before starting to send messages");
               MessageHandler.this.errorListener.fatalError();
            }

         }
      }, START_TIMEOUT, TIME_UNIT);
   }

   public <P> void sendCommand(Message<P, ?> var1, P var2) {
      this.send(var1, var2, MessageHandler.MessageType.COMMAND);
   }

   public <P> void sendHighPriorityReading(Message<P, ?> var1, P var2) {
      this.send(var1, var2, MessageHandler.MessageType.HIGH_PRIORITY_READING);
   }

   public <P> void sendMidPriorityReading(Message<P, ?> var1, P var2) {
      this.send(var1, var2, MessageHandler.MessageType.MID_PRIORITY_READING);
   }

   public <P> void sendLowPriorityReading(Message<P, ?> var1, P var2) {
      this.send(var1, var2, MessageHandler.MessageType.LOW_PRIORITY_READING);
   }

   public void scheduleIdleTimeTask(final Runnable var1) {
      this.executor.submit(new Runnable() {
         public void run() {
            MessageHandler.this.idleTimeTasks.add(var1);
            if (MessageHandler.this.enqueuedMessages.isEmpty()) {
               MessageHandler.this.executeIdleTimeTasks();
            }

         }
      });
   }

   void start() {
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: starting message handler");
      }
      this.executor.submit(new Runnable() {
         public void run() {
            if (!MessageHandler.this.started) {
               MessageHandler.this.started = true;
               MessageHandler.this.sendNext();
            }

         }
      });
   }

   void stop() {
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: stopping message handler");
      }
      this.executor.submit(new Runnable() {
         public void run() {
            MessageHandler.this.stopped = true;
            if (MessageHandler.this.retryFuture != null) {
               MessageHandler.this.retryFuture.cancel(false);
            }
         }
      });
   }

   private <P> void send(final Message<P, ?> var1, final P var2, final MessageHandler.MessageType var3) {
      this.executor.submit(new Runnable() {
         public void run() {
            MessageHandler.this.enqueue(MessageHandler.this.new EnqueuedMessage(var1, var2, var3));
         }
      });
   }

   private void enqueue(MessageHandler.EnqueuedMessage<?> msg) {
      if (this.started && this.enqueuedMessages.isEmpty() && (this.lastMessage == null || this.lastMessage.responseReceived)) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: sending immediately " + msg);
         }
         this.sendMessage(msg);
      } else if (msg.type != MessageHandler.MessageType.COMMAND && this.enqueuedMessages.contains(msg)) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: discarding" + msg);
         }
      } else {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: enqueuing" + msg);
         }
         this.enqueuedMessages.add(msg);
      }
   }

   private void sendNext() {
      MessageHandler.EnqueuedMessage<?> msg = (MessageHandler.EnqueuedMessage)this.enqueuedMessages.poll();
      if (msg != null) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: sending next enqueued message" + msg);
         }
         this.sendMessage(msg);
      } else {
         this.executeIdleTimeTasks();
      }
   }

   private void executeIdleTimeTasks() {
      if (this.started && !this.stopped) {
         Runnable task;
         while((task = (Runnable)this.idleTimeTasks.poll()) != null) {
            try {
               if(VERBOSE_DEBUG) {
                  System.out.println("DEBUG: running idle time task: " + task);
               }
               task.run();
            } catch (RuntimeException ex) {
               System.out.println("ERROR: error running an idle time task: " + ex);
            }
         }
      }
   }

   private void manageError(Integer var1) {
      assert this.started && this.lastMessage != null;
      if (this.lastMessage.type == MessageHandler.MessageType.COMMAND && var1 != null) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: command " + this.lastMessage + " dirscarded");
         }
         this.sendNext();
      } else if (this.lastMessage.attemptNum < RETRY_NUMBER) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: retrying " + this.lastMessage + " ...");
         }
         this.sendMessage(this.lastMessage);
      } else {
         System.out.println("WARN: too many attempts for " + this.lastMessage);
         this.errorListener.fatalError();
      }
   }

   private void sendMessage(final MessageHandler.EnqueuedMessage<?> var1) {
      assert this.started;
      if (!this.stopped) {
         if (this.retryFuture != null) {
            this.retryFuture.cancel(false);
         }
         this.lastMessage = var1;
         this.retryFuture = this.executor.schedule(new Runnable() {
            public void run() {
               assert MessageHandler.this.lastMessage == var1;

               if (!var1.responseReceived) {
                  System.out.println("WARN: response timeout for " + var1);
                  MessageHandler.this.manageError((Integer)null);
               }
            }
         }, RETRY_TIMEOUT, TIME_UNIT);
         this.lastMessage.send();
      }
   }

   private boolean checkResponse(Response var1) {
      if (this.lastMessage != null && this.lastMessage.message == var1.getMessage()) {
         this.lastMessage.responseReceived = true;
         return true;
      } else {
         return false;
      }
   }

   static {
      TIME_UNIT = TimeUnit.MILLISECONDS;
      START_TIMEOUT = TimeUnit.SECONDS.toMillis(40L);
      RETRY_TIMEOUT = TimeUnit.SECONDS.toMillis(5L);
   }

   private class EnqueuedMessage<P> {
      private final Message<P, ?> message;
      private final P param;
      private final MessageHandler.MessageType type;
      private final int n;
      private boolean responseReceived;
      private int attemptNum;

      EnqueuedMessage(Message<P, ?> var2, P var3, MessageHandler.MessageType var4) {
         this.message = (Message)Objects.requireNonNull(var2);
         this.param = var3;
         this.type = (MessageHandler.MessageType)Objects.requireNonNull(var4);
         this.n = MessageHandler.this.messageCount++;
      }

      void send() {
         ++this.attemptNum;
         MessageHandler.this.messenger.send(this.message, this.param);
      }

      public String toString() {
         return String.format("%s(%s) [type: %b, resp: %b, att: %d, n: %d]", this.message, this.param, this.type, this.responseReceived, this.attemptNum, this.n);
      }

      public int hashCode() {
         byte var1 = 7;
         int var2 = 97 * var1 + Objects.hashCode(this.message);
         var2 = 97 * var2 + Objects.hashCode(this.param);
         var2 = 97 * var2 + Objects.hashCode(this.type);
         return var2;
      }

      public boolean equals(Object var1) {
         if (var1 == null) {
            return false;
         } else if (this.getClass() != var1.getClass()) {
            return false;
         } else {
            MessageHandler.EnqueuedMessage<?> var2 = (MessageHandler.EnqueuedMessage)var1;
            if (!Objects.equals(this.message, var2.message)) {
               return false;
            } else if (!Objects.equals(this.param, var2.param)) {
               return false;
            } else {
               return Objects.equals(this.type, var2.type);
            }
         }
      }
   }

   private class MHMessageListener implements MessageListener {
      private MHMessageListener() {
      }

      public void newValue(NewValue var1) {
         if (MessageHandler.this.checkResponse(var1)) {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: response received for " + MessageHandler.this.lastMessage);
            }
            MessageHandler.this.sendNext();
         }
      }

      public void error(DscError var1) {
         if (MessageHandler.this.checkResponse(var1)) {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: error received for " + MessageHandler.this.lastMessage + ": " + var1.getDescription());
            }
            MessageHandler.this.manageError(var1.getResponseCode());
         }
      }
   }

   private class MessageComparator implements Comparator<MessageHandler.EnqueuedMessage<?>> {
      private MessageComparator() {
      }

      public int compare(MessageHandler.EnqueuedMessage<?> var1, MessageHandler.EnqueuedMessage<?> var2) {
         return var1.type != var2.type ? var1.type.compareTo(var2.type) : Integer.compare(var1.n, var2.n);
      }
   }

   private static enum MessageType {
      COMMAND,
      HIGH_PRIORITY_READING,
      MID_PRIORITY_READING,
      LOW_PRIORITY_READING;
   }
}
