package protocol.dsc.messages;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.Priority;
import protocol.dsc.commands.CommandResponse;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.DscGeneralResponse;
import protocol.dsc.session.SendingMessage;

import java.util.ArrayList;
import java.util.List;

abstract class MessageWithResponse<P, V> extends Message<P, V> {

   @SuppressWarnings({"unchecked", "rawtypes"})
   static <P, V> DscCommandWithAppSeq tryToPrepare(
         Class<? extends MessageWithResponse> expectedClass,
         ChannelHandlerContext ctx,
         Object msgObj) throws Exception {
      if (msgObj instanceof SendingMessage) {
         SendingMessage<P, V> sendingMsg = (SendingMessage<P, V>) msgObj;
         Message<P, V> message = sendingMsg.getMessage();
         if (expectedClass.isInstance(message)) {
               MessageWithResponse<P, V> msgWithResp = (MessageWithResponse<P, V>) message;
               return msgWithResp.doPrepare(ctx, sendingMsg.getParam(), sendingMsg.getPriority());
         }
      }
      return null;
   }

   protected final DscCommandWithAppSeq doPrepare(ChannelHandlerContext ctx, P param, Priority priority) throws Exception {
      DscCommandWithAppSeq command = this.prepareCommand(ctx, param);

      if (command.hasResponseCallback()) {
         throw new IllegalStateException("Callback già presente!");
      }

      command.setResponseCallback(new MessageResponseCallback(ctx, param));
      command.setPriority(priority);
      return command;
   }

   private NewValue newACK(P param) {
      return new NewValue(this, param, null);
   }

   private DscError errorFromDscResponse(P param, DscGeneralResponse response) {
      if (response.isSuccess()) {
         throw new IllegalArgumentException("La risposta dovrebbe essere di errore!");
      }

      Integer responseCode = null;
      if (response instanceof CommandResponse) {
         CommandResponse cmdResp = (CommandResponse) response;
         responseCode = cmdResp.getResponseCode();
      }

      return DscError.newMessageError(this, param, responseCode, response.getDescription());
   }

   protected abstract boolean expectedSuccessfulResponse();

   protected abstract DscCommandWithAppSeq prepareCommand(ChannelHandlerContext ctx, P param) throws Exception;

   protected void parseCommandResponse(ChannelHandlerContext ctx, P param, CommandResponse response, List<Message.Response> outResponses) {
   }

   private class MessageResponseCallback implements DscCommandWithAppSeq.ResponseCallback {
      private final ChannelHandlerContext ctx;
      private final P param;

      MessageResponseCallback(ChannelHandlerContext ctx, P param) {
         this.ctx = ctx;
         this.param = param;
      }

      @Override
      public void generalResponseReceived(Channel channel, DscGeneralResponse response) {
         if (response instanceof CommandResponse) {
               try {
                  CommandResponse cmdResp = (CommandResponse) response;
                  List<Message.Response> parsedResponses = new ArrayList<>();
                  MessageWithResponse.this.parseCommandResponse(this.ctx, this.param, cmdResp, parsedResponses);
                  if (!parsedResponses.isEmpty()) {
                     for (Message.Response resp : parsedResponses) {
                           this.ctx.fireChannelRead(resp);
                     }
                     return;
                  }
               } catch (RuntimeException ex) {
                  System.out.println("ERROR: error parsing command response " + ex);
               }
         }

         if (response.isSuccess()) {
               if (MessageWithResponse.this.expectedSuccessfulResponse()) {
                  this.ctx.fireChannelRead(MessageWithResponse.this.newACK(this.param));
               } else {
                  System.out.println("WARN: unexpected successful response: " + response);
               }
         } else {
               this.ctx.fireChannelRead(MessageWithResponse.this.errorFromDscResponse(this.param, response));
         }
      }
   }
}
