package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import protocol.dsc.util.Crc16;

import java.nio.ByteOrder;

@Sharable
public class DataEncoder extends MessageToByteEncoder<ByteBuf> {
   protected void encode(ChannelHandlerContext var1, ByteBuf var2, ByteBuf var3) throws EncoderException {
      assert var3.order() == ByteOrder.BIG_ENDIAN;

      int var4 = var2.readableBytes() + 2;
      if (var4 >= 4 && var4 <= 32767) {
         if (var4 <= 127) {
            var3.writeByte(var4);
         } else {
            var3.writeShort(var4 | 0x8000);
         }

         var3.writeBytes(var2);
         int var5 = Crc16.CRC_16_CCITT.calculate(var3.slice().retain());
         var3.writeShort(var5);
      } else {
         throw new EncoderException("invalid message length");
      }
   }
}
