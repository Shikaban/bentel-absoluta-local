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

import java.util.logging.Logger;

@Sharable
public class FallbackHandler extends ChannelInboundHandlerAdapter {
   private static final Logger logger = Logger.getLogger(FallbackHandler.class.getName());

   public void exceptionCaught(ChannelHandlerContext context, Throwable exception2) throws Exception {
      context.fireChannelRead(DscError.newFatalError(exception2));
      context.fireUserEventTriggered(SimpleMessage.CLOSING_CHANNEL_EVENT);
      if (exception2 instanceof DscProtocolException) {
         logger.severe("Protocol exception received (sending an error message and closing the channel): " + exception2);
         CommandError commandError2 = new CommandError((DscProtocolException)exception2);
         commandError2.setPriority((Priority)null);
         context.write(commandError2).addListener(ChannelFutureListener.CLOSE).addListener(LogOnFailure.INSTANCE);
      } else {
         logger.severe("Exception received (closing the channel) " + exception2);
         context.close().addListener(LogOnFailure.INSTANCE);
      }
   }

   public void channelRead(ChannelHandlerContext context, Object object2) throws Exception {
      if (object2 == SimpleMessage.COMMAND_RECEIVED) {
         context.write(object2);
      }

      if (object2 instanceof DscCommand) {
         logger.fine("Command received, but not handled: " + object2);
      } else {
         super.channelRead(context, object2);
      }
   }
}
