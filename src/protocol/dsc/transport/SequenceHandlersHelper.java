package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

class SequenceHandlersHelper {
   private static final AttributeKey<SequenceHandlersHelper.Counters> COUNTERS_KEY = AttributeKey.valueOf("SequenceHandlersHelper.counters");

   static int next(int var0) {
      return var0 < 255 ? var0 + 1 : 1;
   }

   static boolean isLowACK(ByteBuf var0) {
      return !var0.isReadable();
   }

   static SequenceHandlersHelper.Counters getCounters(ChannelHandlerContext var0) {
      Attribute<SequenceHandlersHelper.Counters> var1 = var0.channel().attr(COUNTERS_KEY);
      SequenceHandlersHelper.Counters var2 = (SequenceHandlersHelper.Counters)var1.get();

      assert var2 == null || var2.thread == Thread.currentThread();

      if (var2 == null) {
         var2 = new SequenceHandlersHelper.Counters();
         var1.set(var2);
      }

      return var2;
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

      void setNextSequenceNumber(boolean var1) {
         if (!var1 && !this.firstMessage) {
            this.prevSequenceNumber = this.sequenceNumber;
            this.sequenceNumber = SequenceHandlersHelper.next(this.sequenceNumber);
         }

         this.firstMessage = false;
      }

      void messageReceived(boolean var1, int var2, int var3) {
         if (!var1) {
            this.remoteSequenceNumber = var2;
         }

         this.lastReceivedACK = var3;
         this.firstMessage = false;
      }

      void setSentRemoteSequenceNumber(int var1) {
         this.lastSentACK = var1;
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
      Counters(Object var1) {
         this();
      }
   }
}
