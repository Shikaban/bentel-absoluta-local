package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;

public abstract class Writing<P> extends MessageWithResponse<P, Void> {
   Writing() {
   }

   public static DscCommandWithAppSeq tryToPrepare(ChannelHandlerContext var0, Object var1) throws Exception {
      return tryToPrepare(Writing.class, var0, var1);
   }

   protected final boolean expectedSuccessfulResponse() {
      return true;
   }
}
