package protocol.dsc.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class BCDCharset extends Charset {
   public BCDCharset() {
      super("BCD", (String[])null);
   }

   public boolean contains(Charset var1) {
      return this.equals(var1);
   }

   public CharsetDecoder newDecoder() {
      return new CharsetDecoder(this, 2.0F, 2.0F) {
         protected CoderResult decodeLoop(ByteBuffer var1, CharBuffer var2) {
            while(var1.remaining() > 0 && var2.remaining() > 0) {
               byte var3 = var1.get();
               var2.append(this.toChar(var3 >> 4 & 15));
               var2.append(this.toChar(var3 & 15));
            }

            return var1.remaining() > 0 ? CoderResult.OVERFLOW : CoderResult.UNDERFLOW;
         }

         private char toChar(int var1) {
            return (char)(var1 < 10 ? 48 + var1 : 65 + (var1 - 10));
         }
      };
   }

   public CharsetEncoder newEncoder() {
      return new CharsetEncoder(this, 0.5F, 1.0F, new byte[1]) {
         private final int[] b = new int[2];
         private int i;

         protected void implReset() {
            this.i = 0;
         }

         protected CoderResult implFlush(ByteBuffer var1) {
            if (this.i == 1) {
               var1.put((byte)(this.b[0] << 4));
            }

            return CoderResult.UNDERFLOW;
         }

         protected CoderResult encodeLoop(CharBuffer var1, ByteBuffer var2) {
            while(var1.remaining() > 0 && var2.remaining() > 0) {
               char var3 = var1.get();
               if ('0' <= var3 && var3 <= '9') {
                  this.b[this.i] = var3 - 48 & 15;
               } else if (var3 >= 'A' && var3 <= 'F') {
                  this.b[this.i] = var3 - 65 + 10 & 15;
               } else {
                  if (var3 < 'a' || var3 > 'f') {
                     continue;
                  }

                  this.b[this.i] = var3 - 97 + 10 & 15;
               }

               if (this.i == 1) {
                  var2.put((byte)(this.b[0] << 4 | this.b[1]));
               }

               this.i ^= 1;
            }

            return var1.remaining() > 0 ? CoderResult.OVERFLOW : CoderResult.UNDERFLOW;
         }
      };
   }
}
