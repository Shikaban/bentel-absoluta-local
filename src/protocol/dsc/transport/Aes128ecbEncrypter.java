package protocol.dsc.transport;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.AttributeKey;
import java.nio.ByteBuffer;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Sharable
public class Aes128ecbEncrypter extends MessageToMessageEncoder<ByteBuf> {
   private static final int KEY_SIZE = 16;
   public static final int BLOCK_SIZE = 16;
   public static final AttributeKey<byte[]> ENCRYPT_KEY = AttributeKey.valueOf("Aes128ecbEncrypter.key");

   protected void encode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws Exception {
      byte[] var4 = (byte[])var1.channel().attr(ENCRYPT_KEY).get();
      if (var4 != null) {
         Preconditions.checkArgument(var4.length == KEY_SIZE);
         Preconditions.checkArgument(var2.readableBytes() % BLOCK_SIZE == 0);
         ByteBuffer var5 = var2.nioBuffer();
         ByteBuffer var6 = encrypt(var4, var5);
         var3.add(Unpooled.wrappedBuffer(var6));
      } else {
         var3.add(var2.retain());
      }

   }

   private static ByteBuffer encrypt(byte[] var0, ByteBuffer var1) throws Exception {
      ByteBuffer var2 = ByteBuffer.allocate(var1.remaining());
      Cipher var3 = Cipher.getInstance("AES/ECB/NoPadding");
      SecretKeySpec var4 = new SecretKeySpec(var0, "AES");
      var3.init(1, var4);
      var3.doFinal(var1, var2);
      return (ByteBuffer)var2.flip();
   }
}
