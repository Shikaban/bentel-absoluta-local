package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;

import protocol.dsc.errors.WrongSequenceNumberException;

import java.util.List;
import java.util.logging.Logger;

@Sharable
public class TransportLayerDecoder extends MessageToMessageDecoder<ByteBuf> {
   private static final Logger logger = Logger.getLogger(TransportLayerDecoder.class.getName());

   protected void decode(ChannelHandlerContext context, ByteBuf inputBuffer, List<Object> output) throws WrongSequenceNumberException, CorruptedFrameException {

      if (inputBuffer.readableBytes() != 2 && inputBuffer.readableBytes() < 4) {
         throw new CorruptedFrameException("invalid frame lenght");
      } else {
         short byteValue = inputBuffer.readUnsignedByte();
         short byteValue2 = inputBuffer.readUnsignedByte();
         boolean lowACK = SequenceHandlersHelper.isLowACK(inputBuffer);
         SequenceHandlersHelper.Counters counters2 = SequenceHandlersHelper.getCounters(context);
         int unsignedShort = lowACK ? 0 : inputBuffer.getUnsignedShort(inputBuffer.readerIndex());

         try {
            if (!lowACK) {
               if (byteValue == 0) {
                  if (!counters2.isFirstMessage()) {
                     throw new WrongSequenceNumberException(unsignedShort, "reset request");
                  }
               } else {
                  if (byteValue == counters2.remoteSequenceNumber()) {
                     logger.fine("Repeated sequence number " + Integer.valueOf(byteValue) + ": ignoring message");
                     return;
                  }

                  if (byteValue != SequenceHandlersHelper.next(counters2.remoteSequenceNumber())) {
                     throw new WrongSequenceNumberException(unsignedShort, String.format("unexpected sequence number: %d instead of %d", Integer.valueOf(byteValue), SequenceHandlersHelper.next(counters2.remoteSequenceNumber())));
                  }
               }
            }

            if (byteValue2 != counters2.sequenceNumber() && byteValue2 != counters2.prevSequenceNumber()) {
               throw new WrongSequenceNumberException(unsignedShort, String.format("unexpected remote sequence number: %d instead of %d or %d", Integer.valueOf(byteValue2), counters2.sequenceNumber(), counters2.prevSequenceNumber()));
            } else {
               output.add(inputBuffer.readSlice(inputBuffer.readableBytes()).retain());
            }
         } finally {
            counters2.messageReceived(lowACK, byteValue, byteValue2);
         }
      }
   }
}