package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.CorruptedFrameException;
import protocol.dsc.util.DscUtils;

import java.io.IOException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

class AESHelper {
   static final int DECRYPT_MODE = 2;
   static final int ENCRYPT_MODE = 1;
   private static final int BLOCK_SIZE = 16;
   private final byte[] buffer = new byte[128];
   private final int mode;
   private final Cipher cipher;

   static AESHelper getInstance(byte[] byteValue, int numericValue) {
      return byteValue == null ? null : new AESHelper(byteValue, numericValue);
   }

   private AESHelper(byte[] byteValue, int mode) {
      if (mode != 2 && mode != 1) {
         throw new IllegalArgumentException("invalid mode " + mode);
      } else {
         this.mode = mode;
         this.cipher = DscUtils.getAESCipher(byteValue, mode);

         assert this.cipher.getBlockSize() == BLOCK_SIZE;

         assert this.buffer.length >= BLOCK_SIZE;

      }
   }

   void process(ByteBuf buffer2, ByteBuf buffer3) {
      try {
         int paddingLength = (BLOCK_SIZE - buffer2.readableBytes() % BLOCK_SIZE) % BLOCK_SIZE;

         assert 0 <= paddingLength && paddingLength < BLOCK_SIZE;

         assert (buffer2.readableBytes() + paddingLength) % BLOCK_SIZE == 0;

         if (this.mode == 2 && paddingLength != 0) {
            throw new CorruptedFrameException(String.format("length must be multiple of %d bytes", BLOCK_SIZE));
         } else {
            ByteBufInputStream byteBufInputStream2 = new ByteBufInputStream(buffer2);
            Throwable exception2 = null;

            try {
               CipherOutputStream cipherOutputStream2 = new CipherOutputStream(new ByteBufOutputStream(buffer3), this.cipher);
               Throwable exception3 = null;

               try {
                  int bytesRead;
                  while((bytesRead = byteBufInputStream2.read(this.buffer)) != -1) {
                     cipherOutputStream2.write(this.buffer, 0, bytesRead);
                  }

                  cipherOutputStream2.write(this.buffer, 0, paddingLength);
               } catch (Throwable exception4) {
                  exception3 = exception4;
                  throw exception4;
               } finally {
                  if (cipherOutputStream2 != null) {
                     if (exception3 != null) {
                        try {
                           cipherOutputStream2.close();
                        } catch (Throwable exception5) {
                           exception3.addSuppressed(exception5);
                        }
                     } else {
                        cipherOutputStream2.close();
                     }
                  }

               }
            } catch (Throwable exception6) {
               exception2 = exception6;
               throw exception6;
            } finally {
               if (byteBufInputStream2 != null) {
                  if (exception2 != null) {
                     try {
                        byteBufInputStream2.close();
                     } catch (Throwable exception7) {
                        exception2.addSuppressed(exception7);
                     }
                  } else {
                     byteBufInputStream2.close();
                  }
               }

            }
         }
      } catch (IOException exception8) {
         throw new RuntimeException("unexpected exception", exception8);
      }
   }
}
