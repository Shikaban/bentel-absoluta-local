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

   protected void decode(ChannelHandlerContext context, ByteBuf inputBuffer, List<Object> output) throws CorruptedFrameException {
      AESHelper aESHelper2 = (AESHelper)context.channel().attr(HELPER_KEY).get();
      if (aESHelper2 != null) {
         ByteBuf inputBuffer2 = context.alloc().buffer(inputBuffer.readableBytes());
         aESHelper2.process(inputBuffer, inputBuffer2);
         output.add(inputBuffer2);
      } else {
         output.add(inputBuffer.retain());
      }

   }

   public static void setKey(Channel channel2, byte[] byteValue) {
      AESHelper instance = AESHelper.getInstance(byteValue, 2);
      channel2.attr(HELPER_KEY).set(instance);
   }
}
