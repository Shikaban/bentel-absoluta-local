package protocol.dsc.transport.command_handlers;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.commands.EndSession;
import protocol.dsc.commands.LowACK;
import protocol.dsc.transport.SimpleMessage;

@Sharable
public class EndSessionHandler extends ChannelDuplexHandler {

   public void write(ChannelHandlerContext var1, Object var2, ChannelPromise var3) throws Exception {
      if (var2 instanceof EndSession) {
         System.out.println("DEBUG: sending end session");
         var1.fireUserEventTriggered(SimpleMessage.CLOSING_CHANNEL_EVENT);
      }

      super.write(var1, var2, var3);
   }

   public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
      if (var2 instanceof EndSession) {
         System.out.println("DEBUG: end session request received");
         var1.fireUserEventTriggered(SimpleMessage.CLOSING_CHANNEL_EVENT);
         var1.write(LowACK.getInstance()).addListener(ChannelFutureListener.CLOSE);
      } else {
         super.channelRead(var1, var2);
      }

   }
}
