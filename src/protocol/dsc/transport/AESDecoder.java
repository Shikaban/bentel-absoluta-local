package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.AttributeKey;
import java.util.List;

@Sharable
public class AESDecoder extends MessageToMessageDecoder<ByteBuf> {
   private static final AttributeKey<AESHelper> HELPER_KEY = AttributeKey.valueOf("AESDecoder.helper");

   protected void decode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws CorruptedFrameException {
      AESHelper var4 = (AESHelper)var1.channel().attr(HELPER_KEY).get();
      if (var4 != null) {
         ByteBuf var5 = var1.alloc().buffer(var2.readableBytes());
         var4.process(var2, var5);
         var3.add(var5);
      } else {
         var3.add(var2.retain());
      }

   }

   public static void setKey(Channel var0, byte[] var1) {
      AESHelper var2 = AESHelper.getInstance(var1, 2);
      var0.attr(HELPER_KEY).set(var2);
   }
}
