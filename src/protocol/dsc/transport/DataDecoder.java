package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;

import protocol.dsc.util.Crc16;
import protocol.dsc.util.Decoders;

import java.util.List;

@Sharable
public class DataDecoder extends MessageToMessageDecoder<ByteBuf> {

   protected void decode(ChannelHandlerContext context, ByteBuf inputBuffer, List<Object> output) throws CorruptedFrameException {

      if (inputBuffer.readableBytes() < 5) {
         throw new CorruptedFrameException("too short frame");
      } else {
         int frameStartIndex = inputBuffer.readerIndex();
         int bytesToFollow = Decoders.readBytesToFollow(inputBuffer);
         if (inputBuffer.readableBytes() < bytesToFollow) {
            throw new CorruptedFrameException("wrong frame length");
         } else {
            ByteBuf frameData = inputBuffer.readSlice(bytesToFollow - 2);
            int crcPosition = inputBuffer.readerIndex();
            int receivedCrc = inputBuffer.readUnsignedShort();
            int calculatedCrc = Crc16.CRC_16_CCITT.calculate(inputBuffer.slice(frameStartIndex, crcPosition - frameStartIndex).retain());
            if (receivedCrc == calculatedCrc) {
               output.add(frameData.retain());
            } else {
               throw new CorruptedFrameException(String.format("wrong crc (0x%04X instead of 0x%04X)", calculatedCrc, receivedCrc));
            }
         }
      }
   }
}
