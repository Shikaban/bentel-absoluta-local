package protocol.dsc.base;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.Arrays;

public final class DscNumber implements DscSerializable {
   private final byte[] bytes;
   private final boolean signed;


   public DscNumber(int numBytes, boolean signed) {
      Preconditions.checkArgument(1 <= numBytes && numBytes <= 4);
      this.bytes = new byte[numBytes];
      this.signed = signed;
   }

   public int size() {
      return this.bytes.length;
   }

   public boolean isSigned() {
      return this.signed;
   }

   public long get() {
      long value;
      if (this.signed) {
         value = (long)this.bytes[0];
      } else {
         value = (long)(this.bytes[0] & 0xFF);
      }

      for (int i = 1; i < this.bytes.length; ++i) {
         value <<= 8;
         value |= (long)(this.bytes[i] & 0xFF);
      }

      return value;
   }

   public int toInt() {
      assert this.bytes.length <= 4;

      if (this.bytes.length == 4 && !this.signed) {
         throw new UnsupportedOperationException("number too long for an int");
      } else {
         return (int)this.get();
      }
   }

   public DscNumber set(long value) {
      if (value >= this.min() && value <= this.max()) {
         for (int i = this.bytes.length - 1; i >= 0; --i) {
               this.bytes[i] = (byte)((int)value);
               value >>= 8;
         }
         return this;
      } else {
         throw new IllegalArgumentException(String.format("%d not in range [%d, %d]", value, this.min(), this.max()));
      }
   }

   public long min() {
      return this.signed ? -1L << (8 * this.bytes.length - 1) : 0L;
   }

   public long max() {
      return -1L >>> (64 - 8 * this.bytes.length + (this.signed ? 1 : 0));
   }

   public String toString() {
      return Long.toString(this.get());
   }

   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         DscNumber other = (DscNumber)obj;
         return Arrays.equals(this.bytes, other.bytes) && this.signed == other.signed;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes) ^ (this.signed ? -1 : 0);
   }

   public void readFrom(ByteBuf buf) throws IndexOutOfBoundsException, DecoderException {
      buf.readBytes(this.bytes);
   }

   public void writeTo(ByteBuf buf) {
      buf.writeBytes(this.bytes);
   }

   public static DscNumber newSignedNum(int numBytes) {
      return new DscNumber(numBytes, true);
   }

   public static DscNumber newUnsignedNum(int numBytes) {
      return new DscNumber(numBytes, false);
   }

   public boolean isEquivalent(DscSerializable other) {
      if (other instanceof DscNumber) {
         DscNumber otherNum = (DscNumber)other;
         return this.get() == otherNum.get();
      } else {
         return false;
      }
   }
}
