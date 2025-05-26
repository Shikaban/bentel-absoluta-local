package protocol.dsc.transport.command_handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.messages.Writing;

@Sharable
public class WritingHandler extends ChannelOutboundHandlerAdapter {
   public void write(ChannelHandlerContext var1, Object var2, ChannelPromise var3) throws Exception {
      DscCommandWithAppSeq var4 = Writing.tryToPrepare(var1, var2);
      if (var4 != null) {
         var1.write(var4, var3);
      } else {
         super.write(var1, var2, var3);
      }

   }
}
