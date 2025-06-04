package protocol.dsc.base;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import protocol.dsc.util.DscUtils;

import java.nio.charset.Charset;
import java.util.Arrays;

public final class DscBinary implements DscSerializable {
   private final byte[] bytes;

   public DscBinary(int length) {
      this.bytes = new byte[length];
   }

   public DscBinary(byte[] bytes) {
      this.bytes = Preconditions.checkNotNull(bytes);
   }

   public int length() {
      return this.bytes.length;
   }

   public byte[] getBytes() {
      return this.bytes;
   }

   public String toString(Charset charset) {
      return new String(this.bytes, charset);
   }

   @Override
   public String toString() {
      return DscUtils.hexDump(this.bytes);
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         DscBinary other = (DscBinary) obj;
         return Arrays.equals(this.bytes, other.bytes);
      } else {
         return false;
      }
   }

   @Override
   public void readFrom(ByteBuf buffer) throws IndexOutOfBoundsException, DecoderException {
      buffer.readBytes(this.bytes);
   }

   @Override
   public void writeTo(ByteBuf buffer) {
      buffer.writeBytes(this.bytes);
   }

   @Override
   public boolean isEquivalent(DscSerializable other) {
      return this.equals(other);
   }
}
