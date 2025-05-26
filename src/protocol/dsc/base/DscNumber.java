package protocol.dsc.base;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.Arrays;

public final class DscNumber implements DscSerializable {
   private final byte[] bytes;
   private final boolean signed;

   public DscNumber(int var1, boolean var2) {
      Preconditions.checkArgument(1 <= var1 && var1 <= 4);
      this.bytes = new byte[var1];
      this.signed = var2;
   }

   public int size() {
      return this.bytes.length;
   }

   public boolean isSigned() {
      return this.signed;
   }

   public long get() {
      long var1;
      if (this.signed) {
         var1 = (long)this.bytes[0];
      } else {
         var1 = (long)(this.bytes[0] & 255);
      }

      for(int var3 = 1; var3 < this.bytes.length; ++var3) {
         var1 <<= 8;
         var1 |= (long)(this.bytes[var3] & 255);
      }

      return var1;
   }

   public int toInt() {
      assert this.bytes.length <= 4;

      if (this.bytes.length == 4 && !this.signed) {
         throw new UnsupportedOperationException("number too long for an int");
      } else {
         return (int)this.get();
      }
   }

   public DscNumber set(long var1) {
      if (var1 >= this.min() && var1 <= this.max()) {
         for(int var3 = this.bytes.length - 1; var3 >= 0; --var3) {
            this.bytes[var3] = (byte)((int)var1);
            var1 >>= 8;
         }

         return this;
      } else {
         throw new IllegalArgumentException(String.format("%d not in range [%d, %d]", var1, this.min(), this.max()));
      }
   }

   public long min() {
      return this.signed ? -1L << 8 * this.bytes.length - 1 : 0L;
   }

   public long max() {
      return -1L >>> 64 - 8 * this.bytes.length + (this.signed ? 1 : 0);
   }

   public String toString() {
      return Long.toString(this.get());
   }

   public boolean equals(Object var1) {
      if (var1 != null && this.getClass() == var1.getClass()) {
         DscNumber var2 = (DscNumber)var1;
         return Arrays.equals(this.bytes, var2.bytes) && this.signed == var2.signed;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes) ^ (this.signed ? -1 : 0);
   }

   public void readFrom(ByteBuf var1) throws IndexOutOfBoundsException, DecoderException {
      var1.readBytes(this.bytes);
   }

   public void writeTo(ByteBuf var1) {
      var1.writeBytes(this.bytes);
   }

   public static DscNumber newSignedNum(int var0) {
      return new DscNumber(var0, true);
   }

   public static DscNumber newUnsignedNum(int var0) {
      return new DscNumber(var0, false);
   }

   public boolean isEquivalent(DscSerializable var1) {
      if (var1 instanceof DscNumber) {
         DscNumber var2 = (DscNumber)var1;
         return this.get() == var2.get();
      } else {
         return false;
      }
   }
}
