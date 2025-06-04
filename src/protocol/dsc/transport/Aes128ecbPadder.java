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

   protected void encode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws Exception {
      byte[] var4 = (byte[])var1.channel().attr(Aes128ecbEncrypter.ENCRYPT_KEY).get();
      if (var4 != null && var2.readableBytes() % 16 != 0) {
         int var5 = 16 - var2.readableBytes() % 16;

         assert 0 < var5 && var5 < 16;

         byte[] bytes = new byte[var5];
         SECURE_RANDOM.nextBytes(bytes);
         var3.add(Unpooled.wrappedBuffer(new ByteBuf[]{var2.retain(), Unpooled.wrappedBuffer(bytes)}));
      } else {
         var3.add(var2.retain());
      }

   }
}
