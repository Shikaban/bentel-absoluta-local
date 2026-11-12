package protocol.dsc.util;

import io.netty.buffer.ByteBuf;

public final class Decoders {
   public static int readBytesToFollow(ByteBuf inputBuffer) {
      short byteValue = inputBuffer.readUnsignedByte();
      int bytesToFollow;
      if ((byteValue & 128) == 0) {
         bytesToFollow = byteValue;
      } else {
         short byteValue2 = inputBuffer.readUnsignedByte();
         bytesToFollow = (byteValue & 127) << 8 | byteValue2;
      }

      return bytesToFollow;
   }

   private Decoders() {
   }
}
