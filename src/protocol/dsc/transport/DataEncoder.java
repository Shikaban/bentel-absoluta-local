package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

import protocol.dsc.util.Crc16;

@Sharable
public class DataEncoder extends MessageToByteEncoder<ByteBuf> {

   protected void encode(ChannelHandlerContext context, ByteBuf buffer, ByteBuf outputBuffer) throws EncoderException {

      int payloadLength = buffer.readableBytes() + 2;
      if (payloadLength >= 4 && payloadLength <= 32767) {
         if (payloadLength <= 127) {
            outputBuffer.writeByte(payloadLength);
         } else {
            outputBuffer.writeShort(payloadLength | 0x8000);
         }

         outputBuffer.writeBytes(buffer);
         int crc = Crc16.CRC_16_CCITT.calculate(outputBuffer.slice().retain());
         outputBuffer.writeShort(crc);
      } else {
         throw new EncoderException("invalid message length");
      }
   }
}
