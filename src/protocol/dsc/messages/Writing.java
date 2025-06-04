package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;

public abstract class Writing<P> extends MessageWithResponse<P, Void> {

   protected Writing() {
   }

   public static DscCommandWithAppSeq tryToPrepare(ChannelHandlerContext ctx, Object msg) throws Exception {
      return MessageWithResponse.tryToPrepare(Writing.class, ctx, msg);
   }

   @Override
   protected final boolean expectedSuccessfulResponse() {
      return true;
   }
}
