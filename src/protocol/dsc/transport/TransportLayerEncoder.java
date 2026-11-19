package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.logging.Logger;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Sharable
public class TransportLayerEncoder extends MessageToByteEncoder<ByteBuf> {
   private static final Logger logger = Logger.getLogger(TransportLayerEncoder.class.getName());
   private static final int RETRY_COUNT = 4;
   private static final int RETRY_TIMEOUT_SECONDS = 1;
   private static final AttributeKey<PendingRetry> PENDING_RETRY =
         AttributeKey.valueOf("TransportLayerEncoder.pendingRetry");

   protected void encode(ChannelHandlerContext context, ByteBuf buffer, ByteBuf buffer2) {
      boolean lowACK = SequenceHandlersHelper.isLowACK(buffer);
      ByteBuf payload = lowACK ? null : buffer.copy();
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

      if (!lowACK) {
         scheduleRetry(context, sequenceNumber, payload);
      }
   }

   public static void cancelRetry(Channel channel, int acknowledgedSequenceNumber) {
      PendingRetry pending = channel.attr(PENDING_RETRY).get();
      if (pending != null && pending.sequenceNumber == acknowledgedSequenceNumber) {
         if (pending.future != null) {
            pending.future.cancel(false);
         }
         pending.payload.release();
         channel.attr(PENDING_RETRY).set(null);
      }
   }

   private void scheduleRetry(ChannelHandlerContext context, int sequenceNumber, ByteBuf payload) {
      PendingRetry previous = context.channel().attr(PENDING_RETRY).get();
      if (previous != null) {
         if (previous.future != null) {
            previous.future.cancel(false);
         }
         previous.payload.release();
      }

      PendingRetry pending = new PendingRetry(sequenceNumber, payload);
      context.channel().attr(PENDING_RETRY).set(pending);
      pending.future = context.executor().schedule(
            new RetryTask(context, pending), RETRY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
   }

   private static final class PendingRetry {
      private final int sequenceNumber;
      private final ByteBuf payload;
      private int retries;
      private ScheduledFuture<?> future;

      private PendingRetry(int sequenceNumber, ByteBuf payload) {
         this.sequenceNumber = sequenceNumber;
         this.payload = payload;
      }
   }

   private static final class RetryTask implements Runnable {
      private final ChannelHandlerContext context;
      private final PendingRetry pending;

      private RetryTask(ChannelHandlerContext context, PendingRetry pending) {
         this.context = context;
         this.pending = pending;
      }

      @Override
      public void run() {
         PendingRetry current = context.channel().attr(PENDING_RETRY).get();
         if (current != pending || !context.channel().isActive()) {
            return;
         }

         if (pending.retries >= RETRY_COUNT) {
            logger.warning("Packet " + pending.sequenceNumber + " not acknowledged after "
                  + RETRY_COUNT + " retries");
            pending.payload.release();
            context.channel().attr(PENDING_RETRY).set(null);
            context.fireChannelRead(protocol.dsc.DscError.newGenericError(
                  "Application packet not acknowledged after " + RETRY_COUNT + " retries"));
            return;
         }

         pending.retries++;
         SequenceHandlersHelper.Counters counters =
               SequenceHandlersHelper.getCounters(context);
         ByteBuf retryFrame = context.alloc().buffer(pending.payload.readableBytes() + 2);
         retryFrame.writeByte(pending.sequenceNumber);
         retryFrame.writeByte(counters.remoteSequenceNumber());
         retryFrame.writeBytes(pending.payload, pending.payload.readerIndex(),
               pending.payload.readableBytes());
         context.writeAndFlush(retryFrame);
         pending.future = context.executor().schedule(
               this, RETRY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      }
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
