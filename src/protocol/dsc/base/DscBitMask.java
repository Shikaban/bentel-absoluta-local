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

   public DscBitMask(boolean hasLengthByte, int offset, boolean littleEndian) {
      Preconditions.checkArgument(offset >= 0);
      this.fixedSize = false;
      this.lengthByte = hasLengthByte;
      this.bytes = DscUtils.emptyByteArray();
      this.offset = offset;
      this.littleEndian = littleEndian;
   }

   public DscBitMask(boolean hasLengthByte, int offset) {
      this(hasLengthByte, offset, true);
   }

   public DscBitMask(int numBytes, int offset, boolean littleEndian) {
      this(new byte[DscUtils.validateUByte(numBytes)], offset, littleEndian);
   }

   public DscBitMask(int numBytes, int offset) {
      this(numBytes, offset, true);
   }

   public DscBitMask(byte[] bytes, int offset, boolean littleEndian) {
      Preconditions.checkArgument(offset >= 0);
      this.fixedSize = true;
      this.lengthByte = false;
      this.bytes = Preconditions.checkNotNull(bytes);
      this.offset = offset;
      this.littleEndian = littleEndian;
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

   public void setLength(int newLength) {
      if (this.fixedSize) {
         throw new UnsupportedOperationException("fixed size");
      }
      if (newLength != this.bytes.length) {
         this.bytes = new byte[DscUtils.validateUByte(newLength)];
      }
   }

   public int numberOfBits() {
      return this.bytes.length * 8;
   }

   public void setMinNumberOfBits(int minBits) {
      this.setLength((minBits + 7) / 8);
   }

   @Override
   public int size() {
      return this.numberOfBits() + this.offset;
   }

   public byte[] bytes() {
      return this.bytes;
   }

   @Override
   public Boolean set(int index, Boolean value) {
      if (0 <= index && index < this.offset) {
         if (value == null) {
               return null;
         } else {
               throw new IllegalArgumentException("only null is allowed between 0 and " + this.offset);
         }
      } else if (value.equals(this.get(index))) {
         return value;
      } else {
         int bitIndex = index - this.offset;
         int byteIdx = this.byteIndex(bitIndex);
         this.bytes[byteIdx] = (byte) (this.bytes[byteIdx] ^ (1 << (bitIndex & 7)));
         return !value;
      }
   }

   @Override
   public Boolean get(int index) {
      if (0 <= index && index < this.offset) {
         return null;
      } else {
         int bitIndex = index - this.offset;
         int byteIdx = this.byteIndex(bitIndex);
         return ((this.bytes[byteIdx] >> (bitIndex & 7)) & 1) != 0;
      }
   }

   private int byteIndex(int bitIndex) {
      int byteIdx = bitIndex >> 3;
      return this.littleEndian ? byteIdx : this.bytes.length - byteIdx - 1;
   }

   public List<Integer> getTrueIndexes() {
      List<Integer> trueIndexes = new ArrayList<>(8 * this.bytes.length);

      for (int byteIdx = 0; byteIdx < this.bytes.length; ++byteIdx) {
         byte currentByte = this.bytes[this.littleEndian ? byteIdx : this.bytes.length - byteIdx - 1];
         if (currentByte != 0) {
               for (int bit = 0; bit < 8; ++bit) {
                  if ((currentByte & 1) != 0) {
                     trueIndexes.add(8 * byteIdx + bit + this.offset);
                  }
                  currentByte = (byte) (currentByte >>> 1);
               }
         }
      }

      return trueIndexes;
   }

   public void reset() {
      Arrays.fill(this.bytes, (byte) 0);
   }

   public void preset() {
      Arrays.fill(this.bytes, (byte) -1);
   }

   @Override
   public void readFrom(ByteBuf buf) throws IndexOutOfBoundsException, DecoderException {
      if (this.lengthByte) {
         this.setLength(buf.readUnsignedByte());
      } else if (!this.fixedSize) {
         this.setLength(buf.readableBytes());
      }
      buf.readBytes(this.bytes);
   }

   @Override
   public void writeTo(ByteBuf buf) {
      if (this.lengthByte) {
         buf.writeByte(this.bytes.length);
      }
      buf.writeBytes(this.bytes);
   }

   @Override
   public boolean isEquivalent(DscSerializable other) {
      if (!(other instanceof DscBitMask)) {
         return false;
      }
      DscBitMask otherMask = (DscBitMask) other;
      if (this.offset != otherMask.offset || this.littleEndian != otherMask.littleEndian) {
         return false;
      }
      byte[] otherBytes = otherMask.bytes;
      int minLen = Math.min(this.bytes.length, otherBytes.length);

      if (this.littleEndian) {
         for (int i = 0; i < minLen; ++i) {
               if (this.bytes[i] != otherBytes[i]) {
                  return false;
               }
         }
         for (int i = minLen; i < this.bytes.length; ++i) {
               if (this.bytes[i] != 0) {
                  return false;
               }
         }
         for (int i = minLen; i < otherBytes.length; ++i) {
               if (otherBytes[i] != 0) {
                  return false;
               }
         }
      } else {
         for (int i = 1; i <= minLen; ++i) {
               if (this.bytes[this.bytes.length - i] != otherBytes[otherBytes.length - i]) {
                  return false;
               }
         }
         for (int i = 0; i < this.bytes.length - minLen; ++i) {
               if (this.bytes[i] != 0) {
                  return false;
               }
         }
         for (int i = 0; i < otherBytes.length - minLen; ++i) {
               if (otherBytes[i] != 0) {
                  return false;
               }
         }
      }
      return true;
   }
}
