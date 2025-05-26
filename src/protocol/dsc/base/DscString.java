package protocol.dsc.base;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import protocol.dsc.util.DscUtils;

import java.nio.charset.Charset;
import java.util.Arrays;

public final class DscString implements DscSerializable {
   private final boolean fixedSize;
   private final Charset charset;
   private byte[] bytes;

   public DscString(int var1, Charset var2) {
      Preconditions.checkArgument(var1 > 0);
      Preconditions.checkNotNull(var2);
      this.fixedSize = true;
      this.charset = var2;
      this.bytes = new byte[var1];
   }

   public DscString(Charset var1) {
      Preconditions.checkNotNull(var1);
      this.fixedSize = false;
      this.charset = var1;
      this.bytes = DscUtils.emptyByteArray();
   }

   public int length() {
      return this.bytes.length;
   }

   public void setLength(int var1) {
      if (this.fixedSize) {
         throw new UnsupportedOperationException("fixed size");
      } else {
         if (var1 != this.bytes.length) {
            this.bytes = new byte[DscUtils.validateUByte(var1)];
         }

      }
   }

   public boolean isFixedSize() {
      return this.fixedSize;
   }

   public Charset getCharset() {
      return this.charset;
   }

   public byte[] bytes() {
      return this.bytes;
   }

   public DscString setString(String var1) {
      byte[] var2 = var1.getBytes(this.charset);
      if (!this.fixedSize) {
         this.bytes = var2;
      } else {
         if (var2.length > this.bytes.length) {
            throw new IllegalArgumentException(String.format("too long string: %s", var1));
         }

         System.arraycopy(var2, 0, this.bytes, 0, var2.length);
         Arrays.fill(this.bytes, var2.length, this.bytes.length, (byte)0);
      }

      return this;
   }

   public String toString() {
      return new String(this.bytes, this.charset);
   }

   public boolean equals(Object var1) {
      if (var1 != null && this.getClass() == var1.getClass()) {
         DscString var2 = (DscString)var1;
         return Arrays.equals(this.bytes, var2.bytes) && this.charset.equals(var2.charset);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes) ^ this.charset.hashCode();
   }

   public void readFrom(ByteBuf var1) throws IndexOutOfBoundsException, DecoderException {
      if (!this.fixedSize) {
         this.bytes = new byte[var1.readableBytes()];
      }

      var1.readBytes(this.bytes);
   }

   public void writeTo(ByteBuf var1) {
      var1.writeBytes(this.bytes);
   }

   public boolean isEquivalent(DscSerializable var1) {
      if (var1 instanceof DscString) {
         DscString var2 = (DscString)var1;
         return this.toString().equals(var2.toString());
      } else {
         return false;
      }
   }

   public static DscString newBCDString(int var0) {
      return new DscString(var0, DscCharsets.BCD);
   }

   public static DscString newBCDString() {
      return new DscString(DscCharsets.BCD);
   }

   public static DscString newUnicodeString(int var0) {
      return new DscString(var0, DscCharsets.UNICODE);
   }

   public static DscString newUnicodeString() {
      return new DscString(DscCharsets.UNICODE);
   }
}
