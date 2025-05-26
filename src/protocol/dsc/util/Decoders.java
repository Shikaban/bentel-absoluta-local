package protocol.dsc.util;

import io.netty.buffer.ByteBuf;

public final class Decoders {
   public static int readBytesToFollow(ByteBuf var0) {
      short var2 = var0.readUnsignedByte();
      int var1;
      if ((var2 & 128) == 0) {
         var1 = var2;
      } else {
         short var3 = var0.readUnsignedByte();
         var1 = (var2 & 127) << 8 | var3;
      }

      return var1;
   }

   private Decoders() {
   }
}
