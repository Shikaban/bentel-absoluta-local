package protocol.dsc.base;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import protocol.dsc.util.DscUtils;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

public final class DscVariableBytes implements DscSerializable {
   private static final BigInteger MAX_INT = BigInteger.valueOf(2147483647L);
   private byte[] bytes = DscUtils.emptyByteArray();

   public int length() {
      return this.bytes.length;
   }

   public void setLength(int value) {
      if (value != this.bytes.length) {
         this.bytes = new byte[DscUtils.validateUByte(value)];
      }

   }

   public byte[] bytes() {
      return this.bytes;
   }

   public String toString(Charset charset2) {
      return new String(this.bytes, charset2);
   }

   public DscVariableBytes setString(Charset charset2, String text) {
      byte[] bytes2 = text.getBytes(charset2);
      DscUtils.validateUByte(bytes2.length);
      this.bytes = bytes2;
      return this;
   }

   public BigInteger toUnsignedBigInteger() {
      return new BigInteger(1, this.bytes);
   }

   public DscVariableBytes setUnsignedBigInteger(BigInteger unsignedBigInteger) {
      this.bytes = toByteArray(unsignedBigInteger);
      return this;
   }

   public DscVariableBytes setUnsignedBigInteger(BigInteger unsignedBigInteger, int length) {
      byte[] byteValue = toByteArray(unsignedBigInteger);
      if (byteValue.length == length) {
         this.bytes = byteValue;
      } else if (byteValue.length < length) {
         this.setLength(length);
         System.arraycopy(byteValue, 0, this.bytes, length - byteValue.length, byteValue.length);
      } else {
         if (length != 0 || !BigInteger.ZERO.equals(unsignedBigInteger)) {
            throw new IllegalArgumentException("value too big for the given length");
         }

         this.setLength(0);
      }

      return this;
   }

   private static byte[] toByteArray(BigInteger integer) {
      if (integer.signum() < 0) {
         throw new IllegalArgumentException("unexpected negative value");
      } else {
         byte[] byteValue = integer.toByteArray();
         return byteValue.length > 1 && byteValue[0] == 0 ? Arrays.copyOfRange(byteValue, 1, byteValue.length) : byteValue;
      }
   }

   public int toPositiveInt() {
      BigInteger value = this.toUnsignedBigInteger();

      assert value.signum() >= 0;

      if (value.compareTo(MAX_INT) <= 0) {
         return value.intValue();
      } else {
         throw new IllegalStateException("overflow");
      }
   }

   public Integer toPositiveInteger() {
      return this.length() == 0 ? null : this.toPositiveInt();
   }

   public DscVariableBytes setPositiveInt(int positiveInt) {
      if (positiveInt >= 0) {
         this.setUnsignedBigInteger(BigInteger.valueOf((long)positiveInt));
         return this;
      } else {
         throw new IllegalArgumentException("unexpected negative value");
      }
   }

   public DscVariableBytes setPositiveInt(int positiveInt, int length) {
      if (positiveInt >= 0) {
         this.setUnsignedBigInteger(BigInteger.valueOf((long)positiveInt), length);
         return this;
      } else {
         throw new IllegalArgumentException("unexpected negative value");
      }
   }

   public DscVariableBytes setPositiveInteger(Integer positiveInt) {
      if (positiveInt == null) {
         this.setLength(0);
      } else {
         this.setPositiveInt(positiveInt);
      }

      return this;
   }

   public String toString() {
      return DscUtils.hexDump(this.bytes);
   }

   public boolean equals(Object object2) {
      if (object2 != null && this.getClass() == object2.getClass()) {
         DscVariableBytes variableBytes = (DscVariableBytes)object2;
         return Arrays.equals(this.bytes, variableBytes.bytes);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }

   public void readFrom(ByteBuf inputBuffer) throws IndexOutOfBoundsException, DecoderException {
      this.setLength(inputBuffer.readUnsignedByte());
      inputBuffer.readBytes(this.bytes);
   }

   public void writeTo(ByteBuf buffer) {
      buffer.writeByte(this.bytes.length);
      buffer.writeBytes(this.bytes);
   }

   public boolean isEquivalent(DscSerializable serializable) {
      if (serializable instanceof DscVariableBytes) {
         byte[] otherBytes = ((DscVariableBytes)serializable).bytes;
         int commonLength = Math.min(this.bytes.length, otherBytes.length);

         int index;
         for(index = 1; index <= commonLength; ++index) {
            if (this.bytes[this.bytes.length - index] != otherBytes[otherBytes.length - index]) {
               return false;
            }
         }

         for(index = 0; index < this.bytes.length - commonLength; ++index) {
            if (this.bytes[index] != 0) {
               return false;
            }
         }

         for(index = 0; index < otherBytes.length - commonLength; ++index) {
            if (otherBytes[index] != 0) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
