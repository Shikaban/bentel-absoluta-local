package protocol.dsc.transport;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import protocol.dsc.Priority;
import protocol.dsc.commands.DscCommand;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

public class CommandQueueHandler extends ChannelOutboundHandlerAdapter {
   private static final Logger logger = Logger.getLogger(CommandQueueHandler.class.getName());
   private final Map<Priority, Queue<CommandQueueHandler.WaitingMsg>> queues;

   public CommandQueueHandler() {
      Builder<Priority, Queue<CommandQueueHandler.WaitingMsg>> queueBuilder = ImmutableMap.builder();
      Priority[] priorities = Priority.values();
      int priorityCount = priorities.length;

      for(int index = 0; index < priorityCount; ++index) {
         Priority priority = priorities[index];
         queueBuilder.put(priority, new ArrayDeque<CommandQueueHandler.WaitingMsg>());
      }

      this.queues = queueBuilder.build();
   }

   public void handlerRemoved(ChannelHandlerContext context) throws Exception {
      Priority[] priorities = Priority.values();
      int priorityCount = priorities.length;

      for(int index = 0; index < priorityCount; ++index) {
         Priority priority = priorities[index];
         Queue<CommandQueueHandler.WaitingMsg> priorityQueue = this.queues.get(priority);
         if (!priorityQueue.isEmpty()) {
            logger.finer("Removing " + priorityQueue.size() + " enqueued commands with priority " + priority);
            CancellationException cancellationException = new CancellationException("channel inactivated before sending");
            for (CommandQueueHandler.WaitingMsg waitingMessage : priorityQueue) {
               waitingMessage.promise.setFailure(cancellationException);
            }

            priorityQueue.clear();
         }
      }
   }

   public void write(ChannelHandlerContext context, Object object2, ChannelPromise promise) throws Exception {
      if (object2 instanceof DscCommand) {
         DscCommand command = (DscCommand)object2;
         if (command.getPriority() == null) {
            logger.finer("Sending immediately: " + command);
            context.write(command, promise);
         } else {
            this.enqueue(context, command, promise);
            this.tryToSend(context);
         }
      } else {
         this.tryToSend(context);
         super.write(context, object2, promise);
      }

   }

   private void enqueue(ChannelHandlerContext context, DscCommand command, ChannelPromise promise) throws IllegalStateException {
      Priority priority = command.getPriority();
      Queue<CommandQueueHandler.WaitingMsg> priorityQueue = this.queues.get(priority);
      logger.finest("Enqueuing a command with priority " + priority + ": " + command);
      if (priorityQueue.size() < 4096) {
         priorityQueue.add(new CommandQueueHandler.WaitingMsg(command, promise));
      } else {
         logger.warning("Command queue is full");
         IllegalStateException exception = new IllegalStateException("command queue is full");
         context.fireExceptionCaught(exception);
         throw exception;
      }
   }

   private void tryToSend(ChannelHandlerContext context) {
      if (TransportLayerEncoder.isReadyForANewCommand(context)) {
         Priority[] priorities = Priority.values();
         int priorityCount = priorities.length;

         for(int index = 0; index < priorityCount; ++index) {
            Priority priority = priorities[index];
            CommandQueueHandler.WaitingMsg waitingMessage = this.queues.get(priority).poll();
            if (waitingMessage != null) {
               logger.finest("Sending enqueued command with priority " + priority + ": " + waitingMessage.cmd);
               context.write(waitingMessage.cmd, waitingMessage.promise);
               break;
            }
         }
      }
   }

   private static class WaitingMsg {
      final DscCommand cmd;
      final ChannelPromise promise;

      WaitingMsg(DscCommand command, ChannelPromise promise2) {
         this.cmd = command;
         this.promise = promise2;
      }

      public String toString() {
         return "Waiting message: " + this.cmd;
      }
   }
}
