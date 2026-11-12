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

   public boolean contains(Charset charset2) {
      return this.equals(charset2);
   }

   public CharsetDecoder newDecoder() {
      return new CharsetDecoder(this, 2.0F, 2.0F) {
         protected CoderResult decodeLoop(ByteBuffer buffer, CharBuffer charBuffer2) {
            while(buffer.remaining() > 0 && charBuffer2.remaining() > 0) {
               byte byteValue = buffer.get();
               charBuffer2.append(this.toChar(byteValue >> 4 & 15));
               charBuffer2.append(this.toChar(byteValue & 15));
            }

            return buffer.remaining() > 0 ? CoderResult.OVERFLOW : CoderResult.UNDERFLOW;
         }

         private char toChar(int nibble) {
            return (char)(nibble < 10 ? 48 + nibble : 65 + (nibble - 10));
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

         protected CoderResult implFlush(ByteBuffer buffer) {
            if (this.i == 1) {
               buffer.put((byte)(this.b[0] << 4));
            }

            return CoderResult.UNDERFLOW;
         }

         protected CoderResult encodeLoop(CharBuffer charBuffer2, ByteBuffer buffer) {
            while(charBuffer2.remaining() > 0 && buffer.remaining() > 0) {
               char char2 = charBuffer2.get();
               if ('0' <= char2 && char2 <= '9') {
                  this.b[this.i] = char2 - 48 & 15;
               } else if (char2 >= 'A' && char2 <= 'F') {
                  this.b[this.i] = char2 - 65 + 10 & 15;
               } else {
                  if (char2 < 'a' || char2 > 'f') {
                     continue;
                  }

                  this.b[this.i] = char2 - 97 + 10 & 15;
               }

               if (this.i == 1) {
                  buffer.put((byte)(this.b[0] << 4 | this.b[1]));
               }

               this.i ^= 1;
            }

            return charBuffer2.remaining() > 0 ? CoderResult.OVERFLOW : CoderResult.UNDERFLOW;
         }
      };
   }
}
