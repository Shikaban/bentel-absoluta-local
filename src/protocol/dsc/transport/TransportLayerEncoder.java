package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.logging.Logger;

@Sharable
public class TransportLayerEncoder extends MessageToByteEncoder<ByteBuf> {
   private static final Logger logger = Logger.getLogger(TransportLayerEncoder.class.getName());

   protected void encode(ChannelHandlerContext context, ByteBuf buffer, ByteBuf buffer2) {
      boolean lowACK = SequenceHandlersHelper.isLowACK(buffer);
      SequenceHandlersHelper.Counters counters2 = SequenceHandlersHelper.getCounters(context);
      if (!lowACK && !counters2.isReadyForANewCommand()) {
         logger.fine("Command sent before confirmation of the previous");
      }

      counters2.setNextSequenceNumber(lowACK);
      int sequenceNumber = counters2.sequenceNumber();
      int sentRemoteSequenceNumber = counters2.remoteSequenceNumber();
      buffer2.writeByte(sequenceNumber);
      buffer2.writeByte(sentRemoteSequenceNumber);
      buffer2.writeBytes(buffer);
      counters2.setSentRemoteSequenceNumber(sentRemoteSequenceNumber);
   }

   public static boolean isReadyForANewCommand(ChannelHandlerContext context) {
      SequenceHandlersHelper.Counters counters2 = SequenceHandlersHelper.getCounters(context);
      return counters2.isReadyForANewCommand();
   }

   public static boolean isOutgoingACKRequired(ChannelHandlerContext context) {
      SequenceHandlersHelper.Counters counters2 = SequenceHandlersHelper.getCounters(context);
      return counters2.isOutgoingACKRequired();
   }
}
