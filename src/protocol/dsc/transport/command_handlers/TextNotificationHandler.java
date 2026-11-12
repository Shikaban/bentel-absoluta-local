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
   public void channelRead(ChannelHandlerContext context, Object object2) throws Exception {
      if (object2 instanceof TextNotification) {
         TextNotification textNotification2 = (TextNotification)object2;

         try {
            String message2 = textNotification2.getMessage();
            context.fireChannelRead(new NewValue(Message.TEXT_NOTIFICATION, message2));
         } catch (UnsupportedOperationException exception2) {
            context.fireChannelRead(DscError.newMessageError(Message.TEXT_NOTIFICATION, null, exception2));
         }
      } else {
         super.channelRead(context, object2);
      }

   }
}
