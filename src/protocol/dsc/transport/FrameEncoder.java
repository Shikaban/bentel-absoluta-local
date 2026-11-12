package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;
import protocol.dsc.session.SessionInfo;

@Sharable
public class FrameEncoder extends MessageToByteEncoder<ByteBuf> {
   protected void encode(ChannelHandlerContext context, ByteBuf buffer, ByteBuf buffer2) {
      String ownInfo = SessionInfo.getOwnInfo(context.channel()).getMultiPointCommId();
      if (ownInfo != null && !ownInfo.isEmpty()) {
         buffer2.writeBytes(ownInfo.getBytes(FrameHelper.MULTI_POINT_COMM_ID_CHARSET));
      }

      buffer2.writeByte(126);

      while(buffer.isReadable()) {
         byte byteValue = buffer.readByte();
         switch(byteValue) {
         case 125:
         case 126:
         case 127:
            buffer2.writeByte(125);
            buffer2.writeByte((Byte)FrameHelper.ESCAPE_MAP.get(byteValue));
            break;
         default:
            buffer2.writeByte(byteValue);
         }
      }

      buffer2.writeByte(127);
   }
}
