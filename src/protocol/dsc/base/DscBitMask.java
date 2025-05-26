package protocol.dsc.base;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import protocol.dsc.util.DscUtils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;

public final class DscBitMask extends AbstractList<Boolean> implements DscSerializable, RandomAccess {
   private final boolean fixedSize;
   private final boolean lengthByte;
   private final int offset;
   private final boolean littleEndian;
   private byte[] bytes;

   public DscBitMask(boolean var1, int var2, boolean var3) {
      Preconditions.checkArgument(var2 >= 0);
      this.fixedSize = false;
      this.lengthByte = var1;
      this.bytes = DscUtils.emptyByteArray();
      this.offset = var2;
      this.littleEndian = var3;
   }

   public DscBitMask(boolean var1, int var2) {
      this(var1, var2, true);
   }

   public DscBitMask(int var1, int var2, boolean var3) {
      this(new byte[DscUtils.validateUByte(var1)], var2, var3);
   }

   public DscBitMask(int var1, int var2) {
      this(var1, var2, true);
   }

   public DscBitMask(byte[] var1, int var2, boolean var3) {
      Preconditions.checkArgument(var2 >= 0);
      this.fixedSize = true;
      this.lengthByte = false;
      this.bytes = (byte[])Preconditions.checkNotNull(var1);
      this.offset = var2;
      this.littleEndian = var3;
   }

   public int length() {
      return this.bytes.length;
   }

   public boolean isFixedSize() {
      return this.fixedSize;
   }

   public boolean hasLengthByte() {
      return this.lengthByte;
   }

   public int getOffset() {
      return this.offset;
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

   public int numberOfBits() {
      return this.bytes.length * 8;
   }

   public void setMinNumberOfBits(int var1) {
      this.setLength((var1 + 7) / 8);
   }

   public int size() {
      return this.numberOfBits() + this.offset;
   }

   public byte[] bytes() {
      return this.bytes;
   }

   public Boolean set(int var1, Boolean var2) {
      if (0 <= var1 && var1 < this.offset) {
         if (var2 == null) {
            return null;
         } else {
            throw new IllegalArgumentException("only null is allowed between 0 and " + this.offset);
         }
      } else if (var2.equals(this.get(var1))) {
         return var2;
      } else {
         int var3 = var1 - this.offset;
         byte[] var10000 = this.bytes;
         int var10001 = this.byteIndex(var3);
         var10000[var10001] = (byte)(var10000[var10001] ^ 1 << (var3 & 7));
         return !var2;
      }
   }

   public Boolean get(int var1) {
      if (0 <= var1 && var1 < this.offset) {
         return null;
      } else {
         int var2 = var1 - this.offset;
         return (this.bytes[this.byteIndex(var2)] >> (var2 & 7) & 1) != 0;
      }
   }

   private int byteIndex(int var1) {
      int var2 = var1 >> 3;
      return this.littleEndian ? var2 : this.bytes.length - var2 - 1;
   }

   public List<Integer> getTrueIndexes() {
      List<Integer> var1 = new ArrayList(8 * this.bytes.length);

      for(int var2 = 0; var2 < this.bytes.length; ++var2) {
         byte var3 = this.bytes[this.littleEndian ? var2 : this.bytes.length - var2 - 1];
         if (var3 != 0) {
            for(int var4 = 0; var4 < 8; ++var4) {
               if ((var3 & 1) != 0) {
                  var1.add(8 * var2 + var4 + this.offset);
               }

               var3 = (byte)(var3 >>> 1);
            }
         }
      }

      return var1;
   }

   public void reset() {
      Arrays.fill(this.bytes, (byte)0);
   }

   public void preset() {
      Arrays.fill(this.bytes, (byte)-1);
   }

   public void readFrom(ByteBuf var1) throws IndexOutOfBoundsException, DecoderException {
      if (this.lengthByte) {
         this.setLength(var1.readUnsignedByte());
      } else if (!this.fixedSize) {
         this.setLength(var1.readableBytes());
      }

      var1.readBytes(this.bytes);
   }

   public void writeTo(ByteBuf var1) {
      if (this.lengthByte) {
         var1.writeByte(this.bytes.length);
      }

      var1.writeBytes(this.bytes);
   }

   public boolean isEquivalent(DscSerializable var1) {
      if (!(var1 instanceof DscBitMask)) {
         return false;
      } else {
         DscBitMask var2 = (DscBitMask)var1;
         if (this.offset == var2.offset && this.littleEndian == var2.littleEndian) {
            byte[] var3 = var2.bytes;
            int var4 = Math.min(this.bytes.length, var3.length);
            int var5;
            if (this.littleEndian) {
               for(var5 = 0; var5 < var4; ++var5) {
                  if (this.bytes[var5] != var3[var5]) {
                     return false;
                  }
               }

               for(var5 = var4; var5 < this.bytes.length; ++var5) {
                  if (this.bytes[var5] != 0) {
                     return false;
                  }
               }

               for(var5 = var4; var5 < var3.length; ++var5) {
                  if (var3[var5] != 0) {
                     return false;
                  }
               }
            } else {
               for(var5 = 1; var5 <= var4; ++var5) {
                  if (this.bytes[this.bytes.length - var5] != var3[var3.length - var5]) {
                     return false;
                  }
               }

               for(var5 = 0; var5 < this.bytes.length - var4; ++var5) {
                  if (this.bytes[var5] != 0) {
                     return false;
                  }
               }

               for(var5 = 0; var5 < var3.length - var4; ++var5) {
                  if (var3[var5] != 0) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
