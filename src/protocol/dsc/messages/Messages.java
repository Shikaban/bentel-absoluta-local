package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.transport.EndpointHandler;

public final class Messages {
   public static String getPin(ChannelHandlerContext var0) throws IllegalStateException {
      String var1 = EndpointHandler.getPin(var0.channel());
      if (var1 != null) {
         return var1;
      } else {
         throw new IllegalStateException("pin not set");
      }
   }

   private Messages() {
   }
}
