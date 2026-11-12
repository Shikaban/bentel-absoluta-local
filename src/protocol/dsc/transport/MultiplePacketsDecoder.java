package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageDecoder;

import protocol.dsc.errors.WrongCommandLengthException;
import protocol.dsc.util.Decoders;

import java.util.List;

@Sharable
public class MultiplePacketsDecoder extends MessageToMessageDecoder<ByteBuf> {

   protected void decode(ChannelHandlerContext context, ByteBuf inputBuffer, List<Object> output) throws Exception {

      if (inputBuffer.readableBytes() >= 2 && inputBuffer.getShort(inputBuffer.readerIndex()) == 1571) {
         output.add(inputBuffer.readSlice(2).retain());

         try {
            while(inputBuffer.isReadable()) {
               int numericValue = Decoders.readBytesToFollow(inputBuffer);
               output.add(inputBuffer.readSlice(numericValue).retain());
            }
         } catch (IndexOutOfBoundsException exception2) {
            throw new WrongCommandLengthException(1571, "unexpected packet length");
         }
      } else {
         output.add(inputBuffer.retain());
      }

   }
}
