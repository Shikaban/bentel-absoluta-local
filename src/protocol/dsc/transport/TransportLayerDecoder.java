package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;
import protocol.dsc.errors.WrongSequenceNumberException;

import java.nio.ByteOrder;
import java.util.List;

@Sharable
public class TransportLayerDecoder extends MessageToMessageDecoder<ByteBuf> {

   protected void decode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws WrongSequenceNumberException, CorruptedFrameException {
      assert var2.order() == ByteOrder.BIG_ENDIAN;

      if (var2.readableBytes() != 2 && var2.readableBytes() < 4) {
         throw new CorruptedFrameException("invalid frame lenght");
      } else {
         short var4 = var2.readUnsignedByte();
         short var5 = var2.readUnsignedByte();
         boolean var6 = SequenceHandlersHelper.isLowACK(var2);
         SequenceHandlersHelper.Counters var7 = SequenceHandlersHelper.getCounters(var1);
         int var8 = var6 ? 0 : var2.getUnsignedShort(var2.readerIndex());

         try {
            if (!var6) {
               if (var4 == 0) {
                  if (!var7.isFirstMessage()) {
                     throw new WrongSequenceNumberException(var8, "reset request");
                  }
               } else {
                  if (var4 == var7.remoteSequenceNumber()) {
                     System.out.println("WARN: repeated sequence number " + Integer.valueOf(var4) + ": ignoring message");
                     return;
                  }

                  if (var4 != SequenceHandlersHelper.next(var7.remoteSequenceNumber())) {
                     throw new WrongSequenceNumberException(var8, String.format("unexpected sequence number: %d instead of %d", Integer.valueOf(var4), SequenceHandlersHelper.next(var7.remoteSequenceNumber())));
                  }
               }
            }

            if (var5 != var7.sequenceNumber() && var5 != var7.prevSequenceNumber()) {
               throw new WrongSequenceNumberException(var8, String.format("unexpected remote sequence number: %d instead of %d or %d", Integer.valueOf(var5), var7.sequenceNumber(), var7.prevSequenceNumber()));
            } else {
               var3.add(var2.readSlice(var2.readableBytes()).retain());
            }
         } finally {
            var7.messageReceived(var6, var4, var5);
         }
      }
   }
}
