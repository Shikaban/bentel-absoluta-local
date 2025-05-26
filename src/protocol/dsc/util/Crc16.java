package protocol.dsc.util;

import io.netty.buffer.ByteBuf;

public class Crc16 {
   public static final Crc16 CRC_16_CCITT = new Crc16(4129, 65535, 0);
   private final int poly;
   private final int initialVal;
   private final int finalXor;
   private final int[] tab;

   public Crc16(int var1, int var2, int var3) {
      this.poly = DscUtils.validateUShort(var1);
      this.initialVal = DscUtils.validateUShort(var2);
      this.finalXor = DscUtils.validateUShort(var3);
      this.tab = new int[256];

      for(int var4 = 0; var4 < 256; ++var4) {
         this.tab[var4] = this.crc1(var4);
      }

   }

   private int crc1(int var1) {
      int var2 = var1 << 8;

      for(int var3 = 0; var3 < 8; ++var3) {
         if ((var2 & 0x8000) != 0) {
            var2 = (var2 <<= 1) ^ this.poly;
         } else {
            var2 <<= 1;
         }

         var2 &= 65535;
      }

      return var2;
   }

   public int calculate(ByteBuf var1) {
      try {
         int var2;
         short var3;
         int var4;
         int var5;
         for(var2 = this.initialVal; var1.isReadable(); var2 = this.tab[var3 ^ var4] ^ var5 << 8) {
            var3 = var1.readUnsignedByte();
            var4 = var2 >> 8 & 255;
            var5 = var2 & 255;
         }

         int var9 = var2 ^ this.finalXor;
         return var9;
      } finally {
         var1.release();
      }
   }
}
