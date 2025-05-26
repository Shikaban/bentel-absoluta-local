package protocol.dsc.transport.command_handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.TextNotification;

@Sharable
public class TextNotificationHandler extends ChannelInboundHandlerAdapter {
   public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
      if (var2 instanceof TextNotification) {
         TextNotification var3 = (TextNotification)var2;

         try {
            String var4 = var3.getMessage();
            var1.fireChannelRead(new NewValue(Message.TEXT_NOTIFICATION, var4));
         } catch (UnsupportedOperationException var5) {
            var1.fireChannelRead(DscError.newMessageError(Message.TEXT_NOTIFICATION, null, var5));
         }
      } else {
         super.channelRead(var1, var2);
      }

   }
}
