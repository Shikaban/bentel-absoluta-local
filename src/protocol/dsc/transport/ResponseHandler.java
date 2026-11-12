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
import java.util.logging.Logger;

public class ResponseHandler extends ChannelDuplexHandler {
   private static final Logger logger = Logger.getLogger(ResponseHandler.class.getName());
   private static final int MAX_WAITING_CMDS = 32;
   private final Queue<DscCommandWithAppSeq> waitingCmds = new LinkedList<DscCommandWithAppSeq>();
   public void channelInactive(ChannelHandlerContext context) throws Exception {
      if (!this.waitingCmds.isEmpty()) {
         logger.fine("Removing " + this.waitingCmds.size() + " waiting commands");
         this.waitingCmds.clear();
      }
      super.channelInactive(context);
   }

   public void write(ChannelHandlerContext context, Object object2, ChannelPromise promise) throws Exception {
      if (object2 instanceof DscCommandWithAppSeq) {
         DscCommandWithAppSeq commandWithAppSeq = (DscCommandWithAppSeq)object2;
         if (this.waitingCmds.size() == MAX_WAITING_CMDS) {
            DscCommandWithAppSeq commandWithAppSeq2 = (DscCommandWithAppSeq)this.waitingCmds.remove();
            logger.fine("Waiting command removed to limit the queue size: " + commandWithAppSeq2);
         }
         this.waitingCmds.add(commandWithAppSeq);
      }
      super.write(context, object2, promise);
   }

   public void channelRead(ChannelHandlerContext context, Object object2) throws Exception {
      boolean enabled = false;
      if (object2 instanceof DscResponse) {
         DscResponse response2 = (DscResponse)object2;
         for (DscCommandWithAppSeq commandWithAppSeq : waitingCmds) {
            if (commandWithAppSeq.matchAsResponse(response2)) {
               logger.finer("Response " + response2 + " received for " + commandWithAppSeq);
               waitingCmds.remove(commandWithAppSeq);
               enabled = true;
               if (response2 instanceof DscGeneralResponse) {
               DscGeneralResponse generalResponse = (DscGeneralResponse) response2;
               commandWithAppSeq.generalResponseReceived(context.channel(), generalResponse);
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
}