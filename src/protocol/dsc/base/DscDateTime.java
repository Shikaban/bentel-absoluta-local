package protocol.dsc.base;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.Arrays;
import java.util.Calendar;

public final class DscDateTime implements DscSerializable {
   private final byte[] bytes = new byte[]{0, 0, 0, 33};

   public Calendar get() {
      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(Calendar.HOUR_OF_DAY, (this.bytes[0] & 0b11111000) >>> 3);
      calendar.set(Calendar.MINUTE, ((this.bytes[0] & 0b00000111) << 3) | ((this.bytes[1] & 0b11100000) >>> 5));
      calendar.set(Calendar.SECOND, ((this.bytes[1] & 0b00011111) << 1) | ((this.bytes[2] & 0b10000000) >>> 7));
      calendar.set(Calendar.YEAR, ((this.bytes[2] & 0b01111110) >>> 1) + 2000);
      calendar.set(Calendar.MONTH, (((this.bytes[2] & 0b00000001) << 3) | ((this.bytes[3] & 0b11100000) >>> 5)) - 1);
      calendar.set(Calendar.DAY_OF_MONTH, this.bytes[3] & 0b00011111);
      return calendar;
   }

   public DscDateTime set(Calendar calendar) {
      int hour = calendar.get(Calendar.HOUR_OF_DAY);
      int minute = calendar.get(Calendar.MINUTE);
      int second = calendar.get(Calendar.SECOND);
      int year = calendar.get(Calendar.YEAR) - 2000;
      int month = calendar.get(Calendar.MONTH) + 1;
      int day = calendar.get(Calendar.DAY_OF_MONTH);

      this.bytes[0] = (byte) (((hour & 0b00011111) << 3) | ((minute & 0b00111000) >>> 3));
      this.bytes[1] = (byte) (((minute & 0b00000111) << 5) | ((second & 0b00111110) >>> 1));
      this.bytes[2] = (byte) (((second & 0b00000001) << 7) | ((year & 0b00111111) << 1) | ((month & 0b00001000) >> 3));
      this.bytes[3] = (byte) (((month & 0b00000111) << 5) | (day & 0b00011111));
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         DscDateTime other = (DscDateTime) obj;
         return Arrays.equals(this.bytes, other.bytes);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.bytes);
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

   @Override
   public String toString() {
      return this.get().getTime().toString();
   }
}
