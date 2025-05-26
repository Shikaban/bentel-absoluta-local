package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;
import protocol.dsc.session.SessionInfo;

@Sharable
public class FrameEncoder extends MessageToByteEncoder<ByteBuf> {
   protected void encode(ChannelHandlerContext var1, ByteBuf var2, ByteBuf var3) {
      String var4 = SessionInfo.getOwnInfo(var1.channel()).getMultiPointCommId();
      if (var4 != null && !var4.isEmpty()) {
         var3.writeBytes(var4.getBytes(FrameHelper.MULTI_POINT_COMM_ID_CHARSET));
      }

      var3.writeByte(126);

      while(var2.isReadable()) {
         byte var5 = var2.readByte();
         switch(var5) {
         case 125:
         case 126:
         case 127:
            var3.writeByte(125);
            var3.writeByte((Byte)FrameHelper.ESCAPE_MAP.get(var5));
            break;
         default:
            var3.writeByte(var5);
         }
      }

      var3.writeByte(127);
   }
}
