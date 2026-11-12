package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.transport.EndpointHandler;

public final class Messages {
   public static String getPin(ChannelHandlerContext context) throws IllegalStateException {
      String pin2 = EndpointHandler.getPin(context.channel());
      if (pin2 != null) {
         return pin2;
      } else {
         throw new IllegalStateException("pin not set");
      }
   }

   private Messages() {
   }
}
