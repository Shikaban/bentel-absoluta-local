package protocol.dsc.transport;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.commands.DscCommand;
import protocol.dsc.commands.EncapsulatedCommandForMultiplePackets;
import protocol.dsc.commands.LowACK;

@Sharable
public class ACKHandler extends ChannelDuplexHandler {
   private static final boolean VERBOSE_DEBUG = false;

   public void write(ChannelHandlerContext var1, Object var2, ChannelPromise var3) throws Exception {
      if (var2 == SimpleMessage.COMMAND_RECEIVED) {
         if (TransportLayerEncoder.isOutgoingACKRequired(var1)) {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: sending low ACK");
            }
            var1.write(LowACK.getInstance(), var3);
         } else {
            var3.setSuccess();
         }
      } else {
         super.write(var1, var2, var3);
      }
   }

   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (msg instanceof DscCommand) {
         if (msg instanceof LowACK) {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: low ACK received");
            }
         } else if (msg instanceof EncapsulatedCommandForMultiplePackets) {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: multiple packets received");
            }
         } else {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: command received: " + msg);
            }
            ctx.fireChannelRead(msg);
         }

         ctx.fireChannelRead(SimpleMessage.COMMAND_RECEIVED);
      } else {
         super.channelRead(ctx, msg);
      }
   }
}