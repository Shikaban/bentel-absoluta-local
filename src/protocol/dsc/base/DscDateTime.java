package protocol.dsc.base;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.Arrays;
import java.util.Calendar;

public final class DscDateTime implements DscSerializable {
   private final byte[] bytes = new byte[]{0, 0, 0, 33};

   public Calendar get() {
      Calendar var1 = Calendar.getInstance();
      var1.clear();
      var1.set(11, (this.bytes[0] & 248) >>> 3);
      var1.set(12, (this.bytes[0] & 7) << 3 | (this.bytes[1] & 224) >>> 5);
      var1.set(13, (this.bytes[1] & 31) << 1 | (this.bytes[2] & 128) >>> 7);
      var1.set(1, ((this.bytes[2] & 126) >>> 1) + 2000);
      var1.set(2, ((this.bytes[2] & 1) << 3 | (this.bytes[3] & 224) >>> 5) - 1);
      var1.set(5, this.bytes[3] & 31);
      return var1;
   }

   public DscDateTime set(Calendar var1) {
      int var2 = var1.get(11);
      int var3 = var1.get(12);
      int var4 = var1.get(13);
      int var5 = var1.get(1) - 2000;
      int var6 = var1.get(2) + 1;
      int var7 = var1.get(5);
      this.bytes[0] = (byte)((var2 & 31) << 3 | (var3 & 56) >>> 3);
      this.bytes[1] = (byte)((var3 & 7) << 5 | (var4 & 62) >>> 1);
      this.bytes[2] = (byte)((var4 & 1) << 7 | (var5 & 63) << 1 | (var6 & 8) >> 3);
      this.bytes[3] = (byte)((var6 & 7) << 5 | var7 & 31);
      return this;
   }

   public boolean equals(Object var1) {
      if (var1 != null && this.getClass() == var1.getClass()) {
         DscDateTime var2 = (DscDateTime)var1;
         return Arrays.equals(this.bytes, var2.bytes);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes);
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

   public String toString() {
      return this.get().getTime().toString();
   }
}
