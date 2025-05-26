package protocol.dsc.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public final class DscUtils {
   private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

   public static int validateUByte(int var0) throws IllegalArgumentException {
      if (var0 >= 0 && var0 <= 255) {
         return var0;
      } else {
         throw new IllegalArgumentException(String.format("%d out of uint8 range", var0));
      }
   }

   public static int validateUShort(int var0) throws IllegalArgumentException {
      if (var0 >= 0 && var0 <= 65535) {
         return var0;
      } else {
         throw new IllegalArgumentException(String.format("%d out of uint16 range", var0));
      }
   }

   public static long validateUInt(long var0) throws IllegalArgumentException {
      if (var0 >= 0L && var0 <= 4294967295L) {
         return var0;
      } else {
         throw new IllegalArgumentException(String.format("%d out of uint32 range", var0));
      }
   }

   public static Cipher getAESCipher(byte[] var0, int var1) throws IllegalArgumentException {
      try {
         Cipher var2 = Cipher.getInstance("AES/ECB/NoPadding");
         var2.init(var1, new SecretKeySpec(var0, "AES"));
         return var2;
      } catch (InvalidKeyException var3) {
         throw new IllegalArgumentException("invalid key", var3);
      } catch (NoSuchPaddingException | NoSuchAlgorithmException var4) {
         throw new RuntimeException("unexpected exception", var4);
      }
   }

   public static String hexDump(byte[] var0) {
      if (var0 == null) {
         return "null";
      } else if (var0.length == 0) {
         return "";
      } else {
         StringBuilder var1 = new StringBuilder(var0.length * 3);
         var1.append(String.format("%02X", var0[0]));

         for(int var2 = 1; var2 < var0.length; ++var2) {
            var1.append(' ').append(String.format("%02X", var0[var2]));
         }

         return var1.toString();
      }
   }

   public static byte[] emptyByteArray() {
      return EMPTY_BYTE_ARRAY;
   }

   private DscUtils() {
   }
}
