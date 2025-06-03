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

public class CommandQueueHandler extends ChannelOutboundHandlerAdapter {
   private final Map<Priority, Queue<CommandQueueHandler.WaitingMsg>> queues;
   private static final boolean VERBOSE_DEBUG = false;

   public CommandQueueHandler() {
      Builder<Priority, Queue<CommandQueueHandler.WaitingMsg>> var1 = ImmutableMap.builder();
      Priority[] var2 = Priority.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Priority var5 = var2[var4];
         var1.put(var5, new ArrayDeque<CommandQueueHandler.WaitingMsg>());
      }

      this.queues = var1.build();
   }

   public void handlerRemoved(ChannelHandlerContext var1) throws Exception {
      Priority[] var2 = Priority.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Priority var5 = var2[var4];
         Queue<CommandQueueHandler.WaitingMsg> var6 = this.queues.get(var5);
         if (!var6.isEmpty()) {
            System.out.println("WARN: removing " + var6.size() + " enqueued commands with priority " + var5);
            CancellationException var7 = new CancellationException("channel inactivated before sending");
            for (CommandQueueHandler.WaitingMsg var9 : var6) {
               var9.promise.setFailure(var7);
            }

            var6.clear();
         }
      }
   }

   public void write(ChannelHandlerContext var1, Object var2, ChannelPromise var3) throws Exception {
      if (var2 instanceof DscCommand) {
         DscCommand var4 = (DscCommand)var2;
         if (var4.getPriority() == null) {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: sending immediately: " + var4);
            }
            var1.write(var4, var3);
         } else {
            this.enqueue(var1, var4, var3);
            this.tryToSend(var1);
         }
      } else {
         this.tryToSend(var1);
         super.write(var1, var2, var3);
      }

   }

   private void enqueue(ChannelHandlerContext var1, DscCommand var2, ChannelPromise var3) throws IllegalStateException {
      Priority var4 = var2.getPriority();
      Queue<CommandQueueHandler.WaitingMsg> var5 = this.queues.get(var4);
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: enqueuing a command with priority " + var4 + ": " + var2);
      }
      if (var5.size() < 4096) {
         var5.add(new CommandQueueHandler.WaitingMsg(var2, var3));
      } else {
         System.out.println("WARN: command queue is full");
         IllegalStateException var7 = new IllegalStateException("command queue is full");
         var1.fireExceptionCaught(var7);
         throw var7;
      }
   }

   private void tryToSend(ChannelHandlerContext var1) {
      if (TransportLayerEncoder.isReadyForANewCommand(var1)) {
         Priority[] var2 = Priority.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Priority var5 = var2[var4];
            CommandQueueHandler.WaitingMsg var6 = this.queues.get(var5).poll();
            if (var6 != null) {
               if(VERBOSE_DEBUG) {
                  System.out.println("DEBUG: sending enqueued command with priority " + var5 + ": " + var6.cmd);
               }
               var1.write(var6.cmd, var6.promise);
               break;
            }
         }
      }
   }

   private static class WaitingMsg {
      final DscCommand cmd;
      final ChannelPromise promise;

      WaitingMsg(DscCommand var1, ChannelPromise var2) {
         this.cmd = var1;
         this.promise = var2;
      }

      public String toString() {
         return "Waiting message: " + this.cmd;
      }
   }
}
