package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

@Sharable
public class Aes128ecbPadder extends MessageToMessageEncoder<ByteBuf> {
   private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

   protected void encode(ChannelHandlerContext context, ByteBuf buffer, List<Object> output) throws Exception {
      byte[] byteValue = (byte[])context.channel().attr(Aes128ecbEncrypter.ENCRYPT_KEY).get();
      if (byteValue != null && buffer.readableBytes() % 16 != 0) {
         int index = 16 - buffer.readableBytes() % 16;

         assert 0 < index && index < 16;

         byte[] bytes = new byte[index];
         SECURE_RANDOM.nextBytes(bytes);
         output.add(Unpooled.wrappedBuffer(new ByteBuf[]{buffer.retain(), Unpooled.wrappedBuffer(bytes)}));
      } else {
         output.add(buffer.retain());
      }

   }
}
