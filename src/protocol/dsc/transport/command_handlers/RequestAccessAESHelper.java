package protocol.dsc.transport.command_handlers;

import com.google.common.base.Preconditions;

import io.netty.handler.codec.DecoderException;
import protocol.dsc.base.DscVariableBytes;
import protocol.dsc.commands.RequestAccess;
import protocol.dsc.util.DscUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

public class RequestAccessAESHelper {
   public static final int INIT_KEY_LENGTH = 8;
   private static final int KEY_LENGTH = 16;
   private static final int IDENTIFIER_LENGTH = 48;
   private final DscVariableBytes identifier;
   private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

   public RequestAccessAESHelper(RequestAccess var1) {
      this.identifier = var1.identifier();

      assert this.identifier != null;

   }

   public void encryptKey(String var1, byte[] var2) {
      this.encryptKey(var1, var2, getRandomBytes());
   }

   public void encryptKey(String var1, byte[] var2, byte[] var3) {
      Preconditions.checkArgument(var2.length == KEY_LENGTH);
      Preconditions.checkArgument(var3.length == KEY_LENGTH);
      this.identifier.setLength(IDENTIFIER_LENGTH);
      byte[] var4 = this.identifier.bytes();

      for(int var5 = 0; var5 < KEY_LENGTH; ++var5) {
         var4[var5] = var3[var5];
         var4[KEY_LENGTH + 2 * var5] = var3[var5];
         var4[KEY_LENGTH + 2 * var5 + 1] = var2[var5];
      }

      Cipher var8 = getCipher(1, var1);

      try {
         var8.doFinal(var4, KEY_LENGTH, 32, var4, KEY_LENGTH);
      } catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException var7) {
         throw new RuntimeException("unexpected exception", var7);
      }
   }

   public byte[] decryptKey(String var1) {
      try {
         if (this.identifier.length() != IDENTIFIER_LENGTH) {
            throw new DecoderException(String.format("unexpected identifier length (%d istead of %d)", this.identifier.length(), IDENTIFIER_LENGTH));
         } else {
            byte[] var2 = this.identifier.bytes();
            Cipher var3 = getCipher(2, var1);
            var3.doFinal(var2, KEY_LENGTH, 32, var2, KEY_LENGTH);
            byte[] var4 = new byte[16];

            for(int var5 = 0; var5 < KEY_LENGTH; ++var5) {
               byte var6 = var2[var5];
               byte var7 = var2[KEY_LENGTH + 2 * var5];
               if (var6 != var7) {
                  throw new DecoderException(String.format("unexpected byte (0x%02X istead of 0x%02X)", var7, var6));
               }

               var4[var5] = var2[KEY_LENGTH + 2 * var5 + 1];
            }

            return var4;
         }
      } catch (RuntimeException ex) {
         System.out.println("WARN: invalid access request: " + ex);
         return null;
      } catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException ex) {
         throw new RuntimeException("unexpected exception", ex);
      }
   }

   private static Cipher getCipher(int var0, String var1) {
      long var2;
      try {
         var2 = DscUtils.validateUInt(Long.parseLong(var1.substring(0, 8), 16));
      } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
         throw new IllegalArgumentException(String.format("invalid init key '%s': %s", var1, ex.getMessage()), ex);
      }

      byte[] var4 = new byte[16];

      for(int var5 = 0; var5 < 4; ++var5) {
         byte var6 = (byte)((int)(var2 >> 8 * (3 - var5)));

         for(int var7 = 0; var7 < 4; ++var7) {
            var4[4 * var7 + var5] = var6;
         }
      }

      return DscUtils.getAESCipher(var4, var0);
   }

   public static byte[] getRandomBytes() {
      byte[] bytes = new byte[16];
      SECURE_RANDOM.nextBytes(bytes);
      return bytes;
   }
}
