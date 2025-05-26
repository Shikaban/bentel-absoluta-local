package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageDecoder;
import protocol.dsc.errors.WrongCommandLengthException;
import protocol.dsc.util.Decoders;

import java.nio.ByteOrder;
import java.util.List;

@Sharable
public class MultiplePacketsDecoder extends MessageToMessageDecoder<ByteBuf> {
   protected void decode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws Exception {
      assert var2.order() == ByteOrder.BIG_ENDIAN;

      if (var2.readableBytes() >= 2 && var2.getShort(var2.readerIndex()) == 1571) {
         var3.add(var2.readSlice(2).retain());

         try {
            while(var2.isReadable()) {
               int var4 = Decoders.readBytesToFollow(var2);
               var3.add(var2.readSlice(var4).retain());
            }
         } catch (IndexOutOfBoundsException var5) {
            throw new WrongCommandLengthException(1571, "unexpected packet length");
         }
      } else {
         var3.add(var2.retain());
      }

   }
}
