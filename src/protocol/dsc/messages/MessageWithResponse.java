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

   static <P, V> DscCommandWithAppSeq tryToPrepare(Class<? extends MessageWithResponse> var0, ChannelHandlerContext var1, Object var2) throws Exception {
      if (var2 instanceof SendingMessage) {
         SendingMessage<P, V> var3 = (SendingMessage)var2;
         Message<P, V> var4 = var3.getMessage();
         if (var0.isInstance(var4)) {
            MessageWithResponse<P, V> var5 = (MessageWithResponse)var4;
            return var5.doPrepare(var1, var3.getParam(), var3.getPriority());
         }
      }
      return null;
   }

   protected final DscCommandWithAppSeq doPrepare(ChannelHandlerContext var1, P var2, Priority var3) throws Exception {
      DscCommandWithAppSeq var4 = this.prepareCommand(var1, var2);

      assert !var4.hasResponseCallback();

      var4.setResponseCallback(new MessageWithResponse.MessageResponseCallback(var1, var2));
      var4.setPriority(var3);
      return var4;
   }

   private NewValue newACK(P var1) {
      return new NewValue(this, var1, null);
   }

   private DscError errorFromDscResponse(P var1, DscGeneralResponse var2) {
      assert !var2.isSuccess();

      Integer var3 = null;
      if (var2 instanceof CommandResponse) {
         CommandResponse var4 = (CommandResponse)var2;
         var3 = var4.getResponseCode();
      }

      return DscError.newMessageError(this, var1, var3, var2.getDescription());
   }

   protected abstract boolean expectedSuccessfulResponse();

   protected abstract DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, P var2) throws Exception;

   protected void parseCommandResponse(ChannelHandlerContext var1, P var2, CommandResponse var3, List<Message.Response> var4) {
   }

   private class MessageResponseCallback implements DscCommandWithAppSeq.ResponseCallback {
      private final ChannelHandlerContext ctx;
      private final P param;

      MessageResponseCallback(ChannelHandlerContext var2, P var3) {
         this.ctx = var2;
         this.param = var3;
      }

      public void generalResponseReceived(Channel var1, DscGeneralResponse var2) {
         if (var2 instanceof CommandResponse) {
            try {
               CommandResponse var3 = (CommandResponse)var2;
               List<Message.Response> var4 = new ArrayList<Message.Response>();
               MessageWithResponse.this.parseCommandResponse(this.ctx, this.param, var3, var4);
               if (!var4.isEmpty()) {
                  for (Message.Response response : var4) {
                     this.ctx.fireChannelRead(response);
                  }
                  return;
               }
            } catch (RuntimeException ex) {
               System.out.println("ERROR: error parsing command response " + ex);
            }
         }

         if (var2.isSuccess()) {
            if (MessageWithResponse.this.expectedSuccessfulResponse()) {
               this.ctx.fireChannelRead(MessageWithResponse.this.newACK(this.param));
            } else {
               System.out.println("WARN: unexpected succesful response: " + var2);
            }
         } else {
            this.ctx.fireChannelRead(MessageWithResponse.this.errorFromDscResponse(this.param, var2));
         }

      }
   }
}
