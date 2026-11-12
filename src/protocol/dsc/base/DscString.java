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

   public DscString(int length, Charset charset) {
      Preconditions.checkArgument(length > 0);
      Preconditions.checkNotNull(charset);
      this.fixedSize = true;
      this.charset = charset;
      this.bytes = new byte[length];
   }

   public DscString(Charset charset2) {
      Preconditions.checkNotNull(charset2);
      this.fixedSize = false;
      this.charset = charset2;
      this.bytes = DscUtils.emptyByteArray();
   }

   public int length() {
      return this.bytes.length;
   }

   public void setLength(int value) {
      if (this.fixedSize) {
         throw new UnsupportedOperationException("fixed size");
      } else {
         if (value != this.bytes.length) {
            this.bytes = new byte[DscUtils.validateUByte(value)];
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

   public DscString setString(String text) {
      byte[] bytes2 = text.getBytes(this.charset);
      if (!this.fixedSize) {
         this.bytes = bytes2;
      } else {
         if (bytes2.length > this.bytes.length) {
            throw new IllegalArgumentException(String.format("too long string: %s", text));
         }

         System.arraycopy(bytes2, 0, this.bytes, 0, bytes2.length);
         Arrays.fill(this.bytes, bytes2.length, this.bytes.length, (byte)0);
      }

      return this;
   }

   public String toString() {
      return new String(this.bytes, this.charset);
   }

   public boolean equals(Object object2) {
      if (object2 != null && this.getClass() == object2.getClass()) {
         DscString string = (DscString)object2;
         return Arrays.equals(this.bytes, string.bytes) && this.charset.equals(string.charset);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes) ^ this.charset.hashCode();
   }

   public void readFrom(ByteBuf inputBuffer) throws IndexOutOfBoundsException, DecoderException {
      if (!this.fixedSize) {
         this.bytes = new byte[inputBuffer.readableBytes()];
      }

      inputBuffer.readBytes(this.bytes);
   }

   public void writeTo(ByteBuf buffer) {
      buffer.writeBytes(this.bytes);
   }

   public boolean isEquivalent(DscSerializable serializable) {
      if (serializable instanceof DscString) {
         DscString string = (DscString)serializable;
         return this.toString().equals(string.toString());
      } else {
         return false;
      }
   }

   public static DscString newBCDString(int numericValue) {
      return new DscString(numericValue, DscCharsets.BCD);
   }

   public static DscString newBCDString() {
      return new DscString(DscCharsets.BCD);
   }

   public static DscString newUnicodeString(int numericValue) {
      return new DscString(numericValue, DscCharsets.UNICODE);
   }

   public static DscString newUnicodeString() {
      return new DscString(DscCharsets.UNICODE);
   }
}
