package protocol.dsc.base;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import protocol.dsc.util.DscUtils;

import java.nio.charset.Charset;
import java.util.Arrays;

public final class DscBinary implements DscSerializable {
   private final byte[] bytes;

   public DscBinary(int var1) {
      this.bytes = new byte[var1];
   }

   public DscBinary(byte[] var1) {
      this.bytes = (byte[])Preconditions.checkNotNull(var1);
   }

   public int length() {
      return this.bytes.length;
   }

   public byte[] bytes() {
      return this.bytes;
   }

   public String toString(Charset var1) {
      return new String(this.bytes, var1);
   }

   public String toString() {
      return DscUtils.hexDump(this.bytes);
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }

   public boolean equals(Object var1) {
      if (var1 != null && this.getClass() == var1.getClass()) {
         DscBinary var2 = (DscBinary)var1;
         return Arrays.equals(this.bytes, var2.bytes);
      } else {
         return false;
      }
   }

   public void readFrom(ByteBuf var1) throws IndexOutOfBoundsException, DecoderException {
      var1.readBytes(this.bytes);
   }

   public void writeTo(ByteBuf var1) {
      var1.writeBytes(this.bytes);
   }

   public boolean isEquivalent(DscSerializable var1) {
      return this.equals(var1);
   }
}
