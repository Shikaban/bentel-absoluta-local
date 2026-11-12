package protocol.dsc.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public final class DscUtils {
   private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

   public static int validateUByte(int value) throws IllegalArgumentException {
      if (value >= 0 && value <= 255) {
         return value;
      } else {
         throw new IllegalArgumentException(String.format("%d out of uint8 range", value));
      }
   }

   public static int validateUShort(int value) throws IllegalArgumentException {
      if (value >= 0 && value <= 65535) {
         return value;
      } else {
         throw new IllegalArgumentException(String.format("%d out of uint16 range", value));
      }
   }

   public static long validateUInt(long value) throws IllegalArgumentException {
      if (value >= 0L && value <= 4294967295L) {
         return value;
      } else {
         throw new IllegalArgumentException(String.format("%d out of uint32 range", value));
      }
   }

   public static Cipher getAESCipher(byte[] key, int operationMode) throws IllegalArgumentException {
      try {
         Cipher instance = Cipher.getInstance("AES/ECB/NoPadding");
         instance.init(operationMode, new SecretKeySpec(key, "AES"));
         return instance;
      } catch (InvalidKeyException exception2) {
         throw new IllegalArgumentException("invalid key", exception2);
      } catch (NoSuchPaddingException | NoSuchAlgorithmException exception3) {
         throw new RuntimeException("unexpected exception", exception3);
      }
   }

   public static String hexDump(byte[] byteValue) {
      if (byteValue == null) {
         return "null";
      } else if (byteValue.length == 0) {
         return "";
      } else {
         StringBuilder hexDump = new StringBuilder(byteValue.length * 3);
         hexDump.append(String.format("%02X", byteValue[0]));

         for(int index = 1; index < byteValue.length; ++index) {
            hexDump.append(' ').append(String.format("%02X", byteValue[index]));
         }

         return hexDump.toString();
      }
   }

   public static byte[] emptyByteArray() {
      return EMPTY_BYTE_ARRAY;
   }

   private DscUtils() {
   }
}
