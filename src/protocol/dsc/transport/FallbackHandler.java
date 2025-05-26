package protocol.dsc.transport;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.DscError;
import protocol.dsc.Priority;
import protocol.dsc.commands.CommandError;
import protocol.dsc.commands.DscCommand;
import protocol.dsc.errors.DscProtocolException;
import protocol.dsc.util.LogOnFailure;

@Sharable
public class FallbackHandler extends ChannelInboundHandlerAdapter {

   public void exceptionCaught(ChannelHandlerContext var1, Throwable var2) throws Exception {
      var1.fireChannelRead(DscError.newFatalError(var2));
      var1.fireUserEventTriggered(SimpleMessage.CLOSING_CHANNEL_EVENT);
      if (var2 instanceof DscProtocolException) {
         System.out.println("WARN: protocol exception received (sending an error message and closing the channel): " + var2);
         CommandError var3 = new CommandError((DscProtocolException)var2);
         var3.setPriority((Priority)null);
         var1.write(var3).addListener(ChannelFutureListener.CLOSE).addListener(LogOnFailure.INSTANCE);
      } else {
         System.out.println("WARN: exception received (closing the channel) " + var2);
         var1.close().addListener(LogOnFailure.INSTANCE);
      }
   }

   public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
      if (var2 == SimpleMessage.COMMAND_RECEIVED) {
         var1.write(var2);
      }

      if (var2 instanceof DscCommand) {
         System.out.println("DEBUG: command received, but not handled: " + var2);
      } else {
         super.channelRead(var1, var2);
      }
   }
}
