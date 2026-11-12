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

   protected void encode(ChannelHandlerContext context, ByteBuf buffer, ByteBuf buffer2) {
      AESHelper aESHelper2 = (AESHelper)context.channel().attr(HELPER_KEY).get();
      if (aESHelper2 != null) {
         aESHelper2.process(buffer, buffer2);
      } else {
         buffer2.writeBytes(buffer);
      }

   }

   public static void setKey(Channel channel2, byte[] byteValue) {
      AESHelper instance = AESHelper.getInstance(byteValue, 1);
      channel2.attr(HELPER_KEY).set(instance);
   }
}
