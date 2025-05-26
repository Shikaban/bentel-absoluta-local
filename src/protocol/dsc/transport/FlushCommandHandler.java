package protocol.dsc.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.commands.DscCommand;

@Sharable
public class FlushCommandHandler extends ChannelOutboundHandlerAdapter {
   public void write(ChannelHandlerContext var1, Object var2, ChannelPromise var3) throws Exception {
      if (var2 instanceof DscCommand) {
         var1.writeAndFlush(var2, var3);
      } else {
         super.write(var1, var2, var3);
      }

   }
}
