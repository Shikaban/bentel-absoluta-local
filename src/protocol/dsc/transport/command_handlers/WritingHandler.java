package protocol.dsc.transport.command_handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.messages.Writing;

@Sharable
public class WritingHandler extends ChannelOutboundHandlerAdapter {
   public void write(ChannelHandlerContext context, Object object2, ChannelPromise promise) throws Exception {
      DscCommandWithAppSeq commandWithAppSeq = Writing.tryToPrepare(context, object2);
      if (commandWithAppSeq != null) {
         context.write(commandWithAppSeq, promise);
      } else {
         super.write(context, object2, promise);
      }

   }
}
