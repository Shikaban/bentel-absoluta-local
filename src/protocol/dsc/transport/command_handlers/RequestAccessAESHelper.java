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

import java.util.logging.Logger;

public class RequestAccessAESHelper {
   private static final Logger logger = Logger.getLogger(RequestAccessAESHelper.class.getName());
   public static final int INIT_KEY_LENGTH = 8;
   private static final int KEY_LENGTH = 16;
   private static final int IDENTIFIER_LENGTH = 48;
   private final DscVariableBytes identifier;
   private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

   public RequestAccessAESHelper(RequestAccess requestAccess) {
      this.identifier = requestAccess.identifier();

      assert this.identifier != null;

   }

   public void encryptKey(String initializationKey, byte[] sessionKey) {
      this.encryptKey(initializationKey, sessionKey, getRandomBytes());
   }

   public void encryptKey(String initializationKey, byte[] sessionKey, byte[] challengeBytes) {
      Preconditions.checkArgument(sessionKey.length == KEY_LENGTH);
      Preconditions.checkArgument(challengeBytes.length == KEY_LENGTH);
      this.identifier.setLength(IDENTIFIER_LENGTH);
      byte[] identifierBytes = this.identifier.bytes();

      // The clear challenge is repeated and interleaved with the session key in the encrypted part.
      for(int index = 0; index < KEY_LENGTH; ++index) {
         identifierBytes[index] = challengeBytes[index];
         identifierBytes[KEY_LENGTH + 2 * index] = challengeBytes[index];
         identifierBytes[KEY_LENGTH + 2 * index + 1] = sessionKey[index];
      }

      Cipher cipher = getCipher(1, initializationKey);

      try {
         cipher.doFinal(identifierBytes, KEY_LENGTH, 32, identifierBytes, KEY_LENGTH);
      } catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException exception) {
         throw new RuntimeException("unexpected exception", exception);
      }
   }

   public byte[] decryptKey(String initializationKey) {
      try {
         if (this.identifier.length() != IDENTIFIER_LENGTH) {
            throw new DecoderException(String.format("unexpected identifier length (%d istead of %d)", this.identifier.length(), IDENTIFIER_LENGTH));
         } else {
            byte[] identifierBytes = this.identifier.bytes();
            Cipher cipher = getCipher(2, initializationKey);
            cipher.doFinal(identifierBytes, KEY_LENGTH, 32, identifierBytes, KEY_LENGTH);
            byte[] sessionKey = new byte[16];

            for(int index = 0; index < KEY_LENGTH; ++index) {
               byte challengeByte = identifierBytes[index];
               byte repeatedChallengeByte = identifierBytes[KEY_LENGTH + 2 * index];
               if (challengeByte != repeatedChallengeByte) {
                  throw new DecoderException(String.format("unexpected byte (0x%02X istead of 0x%02X)", repeatedChallengeByte, challengeByte));
               }

               sessionKey[index] = identifierBytes[KEY_LENGTH + 2 * index + 1];
            }

            return sessionKey;
         }
      } catch (RuntimeException exception) {
         logger.severe("Invalid access request: " + exception);
         return null;
      } catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException exception) {
         throw new RuntimeException("unexpected exception", exception);
      }
   }

   private static Cipher getCipher(int operationMode, String initializationKey) {
      long initializationKeyValue;
      try {
         initializationKeyValue = DscUtils.validateUInt(Long.parseLong(initializationKey.substring(0, 8), 16));
      } catch (IndexOutOfBoundsException | IllegalArgumentException exception) {
         throw new IllegalArgumentException(String.format("invalid init key '%s': %s", initializationKey, exception.getMessage()), exception);
      }

      byte[] keyBytes = new byte[16];

      for(int index = 0; index < 4; ++index) {
         byte initializationKeyByte = (byte)((int)(initializationKeyValue >> 8 * (3 - index)));

         for(int index2 = 0; index2 < 4; ++index2) {
            keyBytes[4 * index2 + index] = initializationKeyByte;
         }
      }

      return DscUtils.getAESCipher(keyBytes, operationMode);
   }

   public static byte[] getRandomBytes() {
      byte[] randomBytes = new byte[16];
      SECURE_RANDOM.nextBytes(randomBytes);
      return randomBytes;
   }
}