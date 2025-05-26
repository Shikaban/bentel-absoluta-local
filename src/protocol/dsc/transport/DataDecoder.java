package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;
import protocol.dsc.util.Crc16;
import protocol.dsc.util.Decoders;

import java.nio.ByteOrder;
import java.util.List;

@Sharable
public class DataDecoder extends MessageToMessageDecoder<ByteBuf> {
   protected void decode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws CorruptedFrameException {
      assert var2.order() == ByteOrder.BIG_ENDIAN;

      if (var2.readableBytes() < 5) {
         throw new CorruptedFrameException("too short frame");
      } else {
         int var4 = var2.readerIndex();
         int var5 = Decoders.readBytesToFollow(var2);
         if (var2.readableBytes() < var5) {
            throw new CorruptedFrameException("wrong frame length");
         } else {
            ByteBuf var6 = var2.readSlice(var5 - 2);
            int var7 = var2.readerIndex();
            int var8 = var2.readUnsignedShort();
            int var9 = Crc16.CRC_16_CCITT.calculate(var2.slice(var4, var7 - var4).retain());
            if (var8 == var9) {
               var3.add(var6.retain());
            } else {
               throw new CorruptedFrameException(String.format("wrong crc (0x%04X instead of 0x%04X)", var9, var8));
            }
         }
      }
   }
}
