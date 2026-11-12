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

   protected void encode(ChannelHandlerContext context, ByteBuf buffer, List<Object> output) throws Exception {
      byte[] byteValue = (byte[])context.channel().attr(ENCRYPT_KEY).get();
      if (byteValue != null) {
         Preconditions.checkArgument(byteValue.length == KEY_SIZE);
         Preconditions.checkArgument(buffer.readableBytes() % BLOCK_SIZE == 0);
         ByteBuffer buffer2 = buffer.nioBuffer();
         ByteBuffer buffer3 = encrypt(byteValue, buffer2);
         output.add(Unpooled.wrappedBuffer(buffer3));
      } else {
         output.add(buffer.retain());
      }

   }

   private static ByteBuffer encrypt(byte[] byteValue, ByteBuffer buffer) throws Exception {
      ByteBuffer buffer2 = ByteBuffer.allocate(buffer.remaining());
      Cipher instance = Cipher.getInstance("AES/ECB/NoPadding");
      SecretKeySpec secretKeySpec2 = new SecretKeySpec(byteValue, "AES");
      instance.init(1, secretKeySpec2);
      instance.doFinal(buffer, buffer2);
      return (ByteBuffer)buffer2.flip();
   }
}
