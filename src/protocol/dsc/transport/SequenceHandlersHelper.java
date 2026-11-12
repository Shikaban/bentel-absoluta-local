package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

class SequenceHandlersHelper {
   private static final AttributeKey<SequenceHandlersHelper.Counters> COUNTERS_KEY = AttributeKey.valueOf("SequenceHandlersHelper.counters");

   static int next(int sequenceNumber) {
      return sequenceNumber < 255 ? sequenceNumber + 1 : 1;
   }

   static boolean isLowACK(ByteBuf buffer) {
      return !buffer.isReadable();
   }

   static SequenceHandlersHelper.Counters getCounters(ChannelHandlerContext context) {
      Attribute<SequenceHandlersHelper.Counters> attribute2 = context.channel().attr(COUNTERS_KEY);
      SequenceHandlersHelper.Counters counters2 = (SequenceHandlersHelper.Counters)attribute2.get();

      assert counters2 == null || counters2.thread == Thread.currentThread();

      if (counters2 == null) {
         counters2 = new SequenceHandlersHelper.Counters();
         attribute2.set(counters2);
      }

      return counters2;
   }

   private SequenceHandlersHelper() {
   }

   static class Counters {
      private final Thread thread;
      private boolean firstMessage;
      private int prevSequenceNumber;
      private int sequenceNumber;
      private int remoteSequenceNumber;
      private int lastSentACK;
      private int lastReceivedACK;
      private int appSeq;

      private Counters() {
         this.thread = Thread.currentThread();
         this.firstMessage = true;
         this.lastSentACK = -1;
         this.lastReceivedACK = -1;
      }

      int prevSequenceNumber() {
         return this.prevSequenceNumber;
      }

      int sequenceNumber() {
         return this.sequenceNumber;
      }

      int remoteSequenceNumber() {
         return this.remoteSequenceNumber;
      }

      void setNextSequenceNumber(boolean enabled) {
         if (!enabled && !this.firstMessage) {
            this.prevSequenceNumber = this.sequenceNumber;
            this.sequenceNumber = SequenceHandlersHelper.next(this.sequenceNumber);
         }

         this.firstMessage = false;
      }

      void messageReceived(boolean enabled, int remoteSequenceNumber, int lastReceivedACK) {
         if (!enabled) {
            this.remoteSequenceNumber = remoteSequenceNumber;
         }

         this.lastReceivedACK = lastReceivedACK;
         this.firstMessage = false;
      }

      void setSentRemoteSequenceNumber(int value) {
         this.lastSentACK = value;
      }

      boolean isReadyForANewCommand() {
         return this.sequenceNumber == this.lastReceivedACK || this.firstMessage;
      }

      boolean isOutgoingACKRequired() {
         return this.remoteSequenceNumber != this.lastSentACK && !this.firstMessage;
      }

      boolean isFirstMessage() {
         return this.firstMessage;
      }

      int nextAppSeq() {
         this.appSeq = SequenceHandlersHelper.next(this.appSeq);
         return this.appSeq;
      }

      // $FF: synthetic method
      Counters(Object object2) {
         this();
      }
   }
}
