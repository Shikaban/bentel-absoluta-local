package protocol.dsc.transport;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import protocol.dsc.DscError;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.DscGeneralResponse;
import protocol.dsc.commands.DscResponse;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ResponseHandler extends ChannelDuplexHandler {
   private static final Logger logger = Logger.getLogger(ResponseHandler.class.getName());
   private static final int MAX_WAITING_CMDS = 32;
   private static final int RESPONSE_TIMEOUT_SECONDS = 5;
   private final Queue<WaitingCommand> waitingCmds = new LinkedList<WaitingCommand>();
   public void channelInactive(ChannelHandlerContext context) throws Exception {
      if (!this.waitingCmds.isEmpty()) {
         logger.fine("Removing " + this.waitingCmds.size() + " waiting commands");
         for (WaitingCommand waiting : this.waitingCmds) {
            waiting.timeoutFuture.cancel(false);
         }
         this.waitingCmds.clear();
      }
      super.channelInactive(context);
   }

   public void write(ChannelHandlerContext context, Object object2, ChannelPromise promise) throws Exception {
      if (object2 instanceof DscCommandWithAppSeq) {
         DscCommandWithAppSeq commandWithAppSeq = (DscCommandWithAppSeq)object2;
         if (this.waitingCmds.size() == MAX_WAITING_CMDS) {
            WaitingCommand removed = this.waitingCmds.remove();
            removed.timeoutFuture.cancel(false);
            logger.fine("Waiting command removed to limit the queue size: " + removed.command);
         }
         WaitingCommand waiting = new WaitingCommand(commandWithAppSeq);
         this.waitingCmds.add(waiting);
         waiting.timeoutFuture = context.executor().schedule(new Runnable() {
            @Override
            public void run() {
               if (waitingCmds.remove(waiting)) {
                  logger.warning("Application response timeout for " + commandWithAppSeq);
                  context.fireChannelRead(DscError.newGenericError(
                        "Application response timeout after " + RESPONSE_TIMEOUT_SECONDS + " seconds"));
               }
            }
         }, RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      }
      super.write(context, object2, promise);
   }

   public void channelRead(ChannelHandlerContext context, Object object2) throws Exception {
      boolean enabled = false;
      if (object2 instanceof DscResponse) {
         DscResponse response2 = (DscResponse)object2;
         for (WaitingCommand waiting : waitingCmds) {
            if (waiting.command.matchAsResponse(response2)) {
               logger.finer("Response " + response2 + " received for " + waiting.command);
               waiting.timeoutFuture.cancel(false);
               waitingCmds.remove(waiting);
               enabled = true;
               if (response2 instanceof DscGeneralResponse) {
               DscGeneralResponse generalResponse = (DscGeneralResponse) response2;
               waiting.command.generalResponseReceived(context.channel(), generalResponse);
               }
               break;
            }
         }
      }

      if (object2 instanceof DscGeneralResponse) {
         if (!enabled) {
            DscGeneralResponse generalResponse2 = (DscGeneralResponse)object2;
            logger.fine("Unmatched general response " + generalResponse2);
            if (!generalResponse2.isSuccess()) {
               context.fireChannelRead(DscError.newGenericError(generalResponse2.getDescription()));
            }
         }
      } else {
         super.channelRead(context, object2);
      }
   }

   private static final class WaitingCommand {
      private final DscCommandWithAppSeq command;
      private ScheduledFuture<?> timeoutFuture;

      private WaitingCommand(DscCommandWithAppSeq command) {
         this.command = command;
      }
   }
}