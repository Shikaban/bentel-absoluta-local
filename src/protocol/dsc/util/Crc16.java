package protocol.dsc.util;

import io.netty.buffer.ByteBuf;

public class Crc16 {
   public static final Crc16 CRC_16_CCITT = new Crc16(4129, 65535, 0);
   private final int poly;
   private final int initialVal;
   private final int finalXor;
   private final int[] tab;

   public Crc16(int polynomial, int initialValue, int finalXorValue) {
      this.poly = DscUtils.validateUShort(polynomial);
      this.initialVal = DscUtils.validateUShort(initialValue);
      this.finalXor = DscUtils.validateUShort(finalXorValue);
      this.tab = new int[256];

      for(int index = 0; index < 256; ++index) {
         this.tab[index] = this.crc1(index);
      }

   }

   private int crc1(int value) {
      int crc = value << 8;

      for(int index = 0; index < 8; ++index) {
         if ((crc & 0x8000) != 0) {
            crc = (crc <<= 1) ^ this.poly;
         } else {
            crc <<= 1;
         }

         crc &= 65535;
      }

      return crc;
   }

   public int calculate(ByteBuf buffer) {
      try {
         int crc;
         short byteValue;
         int highByte;
         int lowByte;
         for(crc = this.initialVal; buffer.isReadable(); crc = this.tab[byteValue ^ highByte] ^ lowByte << 8) {
            byteValue = buffer.readUnsignedByte();
            highByte = crc >> 8 & 255;
            lowByte = crc & 255;
         }

         int result = crc ^ this.finalXor;
         return result;
      } finally {
         buffer.release();
      }
   }
}
