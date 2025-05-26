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

   static AESHelper getInstance(byte[] var0, int var1) {
      return var0 == null ? null : new AESHelper(var0, var1);
   }

   private AESHelper(byte[] var1, int var2) {
      if (var2 != 2 && var2 != 1) {
         throw new IllegalArgumentException("invalid mode " + var2);
      } else {
         this.mode = var2;
         this.cipher = DscUtils.getAESCipher(var1, var2);

         assert this.cipher.getBlockSize() == BLOCK_SIZE;

         assert this.buffer.length >= BLOCK_SIZE;

      }
   }

   void process(ByteBuf var1, ByteBuf var2) {
      try {
         int var3 = (BLOCK_SIZE - var1.readableBytes() % BLOCK_SIZE) % BLOCK_SIZE;

         assert 0 <= var3 && var3 < BLOCK_SIZE;

         assert (var1.readableBytes() + var3) % BLOCK_SIZE == 0;

         if (this.mode == 2 && var3 != 0) {
            throw new CorruptedFrameException(String.format("length must be multiple of %d bytes", BLOCK_SIZE));
         } else {
            ByteBufInputStream var4 = new ByteBufInputStream(var1);
            Throwable var5 = null;

            try {
               CipherOutputStream var6 = new CipherOutputStream(new ByteBufOutputStream(var2), this.cipher);
               Throwable var7 = null;

               try {
                  int var8;
                  while((var8 = var4.read(this.buffer)) != -1) {
                     var6.write(this.buffer, 0, var8);
                  }

                  var6.write(this.buffer, 0, var3);
               } catch (Throwable var32) {
                  var7 = var32;
                  throw var32;
               } finally {
                  if (var6 != null) {
                     if (var7 != null) {
                        try {
                           var6.close();
                        } catch (Throwable var31) {
                           var7.addSuppressed(var31);
                        }
                     } else {
                        var6.close();
                     }
                  }

               }
            } catch (Throwable var34) {
               var5 = var34;
               throw var34;
            } finally {
               if (var4 != null) {
                  if (var5 != null) {
                     try {
                        var4.close();
                     } catch (Throwable var30) {
                        var5.addSuppressed(var30);
                     }
                  } else {
                     var4.close();
                  }
               }

            }
         }
      } catch (IOException var36) {
         throw new RuntimeException("unexpected exception", var36);
      }
   }
}
