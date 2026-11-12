package protocol.dsc.transport.command_handlers;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;

import protocol.dsc.commands.EndSession;
import protocol.dsc.commands.LowACK;
import protocol.dsc.transport.SimpleMessage;

import java.util.logging.Logger;

@Sharable
public class EndSessionHandler extends ChannelDuplexHandler {
   private static final Logger logger = Logger.getLogger(EndSessionHandler.class.getName());

   public void write(ChannelHandlerContext context, Object object2, ChannelPromise promise) throws Exception {
      if (object2 instanceof EndSession) {
         logger.fine("Sending end session");
         context.fireUserEventTriggered(SimpleMessage.CLOSING_CHANNEL_EVENT);
      }

      super.write(context, object2, promise);
   }

   public void channelRead(ChannelHandlerContext context, Object object2) throws Exception {
      if (object2 instanceof EndSession) {
         logger.info("Mobile App or Boss have requested an end session, request received");
         context.fireUserEventTriggered(SimpleMessage.CLOSING_CHANNEL_EVENT);
         context.write(LowACK.getInstance()).addListener(ChannelFutureListener.CLOSE);
      } else {
         super.channelRead(context, object2);
      }

   }
}
