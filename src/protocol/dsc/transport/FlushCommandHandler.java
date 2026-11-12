package protocol.dsc.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.commands.DscCommand;

@Sharable
public class FlushCommandHandler extends ChannelOutboundHandlerAdapter {
   public void write(ChannelHandlerContext context, Object object2, ChannelPromise promise) throws Exception {
      if (object2 instanceof DscCommand) {
         context.writeAndFlush(object2, promise);
      } else {
         super.write(context, object2, promise);
      }

   }
}
