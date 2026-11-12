package protocol.dsc.transport;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;

import protocol.dsc.commands.DscCommand;
import protocol.dsc.commands.EncapsulatedCommandForMultiplePackets;
import protocol.dsc.commands.LowACK;

import java.util.logging.Logger;

@Sharable
public class ACKHandler extends ChannelDuplexHandler {
   private static final Logger logger = Logger.getLogger(ACKHandler.class.getName());

   public void write(ChannelHandlerContext context, Object object2, ChannelPromise promise) throws Exception {
      if (object2 == SimpleMessage.COMMAND_RECEIVED) {
         if (TransportLayerEncoder.isOutgoingACKRequired(context)) {
            logger.finest("Sending low ACK");
            context.write(LowACK.getInstance(), promise);
         } else {
            promise.setSuccess();
         }
      } else {
         super.write(context, object2, promise);
      }
   }

   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (msg instanceof DscCommand) {
         if (msg instanceof LowACK) {
            logger.finest("Low ACK received");
         } else if (msg instanceof EncapsulatedCommandForMultiplePackets) {
            logger.finer("Multiple packets received");
         } else {
            logger.finest("Command received: " + msg);
            ctx.fireChannelRead(msg);
         }

         ctx.fireChannelRead(SimpleMessage.COMMAND_RECEIVED);
      } else {
         super.channelRead(ctx, msg);
      }
   }
}