package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AttributeKey;

@Sharable
public class AESEncoder extends MessageToByteEncoder<ByteBuf> {
   private static final AttributeKey<AESHelper> HELPER_KEY = AttributeKey.valueOf("AESEncoder.helper");

   protected void encode(ChannelHandlerContext var1, ByteBuf var2, ByteBuf var3) {
      AESHelper var4 = (AESHelper)var1.channel().attr(HELPER_KEY).get();
      if (var4 != null) {
         var4.process(var2, var3);
      } else {
         var3.writeBytes(var2);
      }

   }

   public static void setKey(Channel var0, byte[] var1) {
      AESHelper var2 = AESHelper.getInstance(var1, 1);
      var0.attr(HELPER_KEY).set(var2);
   }
}
