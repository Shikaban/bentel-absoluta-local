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

   public void setLength(int var1) {
      if (var1 != this.bytes.length) {
         this.bytes = new byte[DscUtils.validateUByte(var1)];
      }

   }

   public byte[] bytes() {
      return this.bytes;
   }

   public String toString(Charset var1) {
      return new String(this.bytes, var1);
   }

   public DscVariableBytes setString(Charset var1, String var2) {
      byte[] var3 = var2.getBytes(var1);
      DscUtils.validateUByte(var3.length);
      this.bytes = var3;
      return this;
   }

   public BigInteger toUnsignedBigInteger() {
      return new BigInteger(1, this.bytes);
   }

   public DscVariableBytes setUnsignedBigInteger(BigInteger var1) {
      this.bytes = toByteArray(var1);
      return this;
   }

   public DscVariableBytes setUnsignedBigInteger(BigInteger var1, int var2) {
      byte[] var3 = toByteArray(var1);
      if (var3.length == var2) {
         this.bytes = var3;
      } else if (var3.length < var2) {
         this.setLength(var2);
         System.arraycopy(var3, 0, this.bytes, var2 - var3.length, var3.length);
      } else {
         if (var2 != 0 || !BigInteger.ZERO.equals(var1)) {
            throw new IllegalArgumentException("value too big for the given length");
         }

         this.setLength(0);
      }

      return this;
   }

   private static byte[] toByteArray(BigInteger var0) {
      if (var0.signum() < 0) {
         throw new IllegalArgumentException("unexpected negative value");
      } else {
         byte[] var1 = var0.toByteArray();
         return var1.length > 1 && var1[0] == 0 ? Arrays.copyOfRange(var1, 1, var1.length) : var1;
      }
   }

   public int toPositiveInt() {
      BigInteger var1 = this.toUnsignedBigInteger();

      assert var1.signum() >= 0;

      if (var1.compareTo(MAX_INT) <= 0) {
         return var1.intValue();
      } else {
         throw new IllegalStateException("overflow");
      }
   }

   public Integer toPositiveInteger() {
      return this.length() == 0 ? null : this.toPositiveInt();
   }

   public DscVariableBytes setPositiveInt(int var1) {
      if (var1 >= 0) {
         this.setUnsignedBigInteger(BigInteger.valueOf((long)var1));
         return this;
      } else {
         throw new IllegalArgumentException("unexpected negative value");
      }
   }

   public DscVariableBytes setPositiveInt(int var1, int var2) {
      if (var1 >= 0) {
         this.setUnsignedBigInteger(BigInteger.valueOf((long)var1), var2);
         return this;
      } else {
         throw new IllegalArgumentException("unexpected negative value");
      }
   }

   public DscVariableBytes setPositiveInteger(Integer var1) {
      if (var1 == null) {
         this.setLength(0);
      } else {
         this.setPositiveInt(var1);
      }

      return this;
   }

   public String toString() {
      return DscUtils.hexDump(this.bytes);
   }

   public boolean equals(Object var1) {
      if (var1 != null && this.getClass() == var1.getClass()) {
         DscVariableBytes var2 = (DscVariableBytes)var1;
         return Arrays.equals(this.bytes, var2.bytes);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }

   public void readFrom(ByteBuf var1) throws IndexOutOfBoundsException, DecoderException {
      this.setLength(var1.readUnsignedByte());
      var1.readBytes(this.bytes);
   }

   public void writeTo(ByteBuf var1) {
      var1.writeByte(this.bytes.length);
      var1.writeBytes(this.bytes);
   }

   public boolean isEquivalent(DscSerializable var1) {
      if (var1 instanceof DscVariableBytes) {
         byte[] var2 = ((DscVariableBytes)var1).bytes;
         int var3 = Math.min(this.bytes.length, var2.length);

         int var4;
         for(var4 = 1; var4 <= var3; ++var4) {
            if (this.bytes[this.bytes.length - var4] != var2[var2.length - var4]) {
               return false;
            }
         }

         for(var4 = 0; var4 < this.bytes.length - var3; ++var4) {
            if (this.bytes[var4] != 0) {
               return false;
            }
         }

         for(var4 = 0; var4 < var2.length - var3; ++var4) {
            if (var2[var4] != 0) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
