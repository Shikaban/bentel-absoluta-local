package protocol.dsc.messages;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.DscResponse;

import java.util.List;

public abstract class Reading<P, V, C extends DscResponse> extends MessageWithResponse<P, V> {
   private static Multimap<Class<? extends DscResponse>, Reading<?, ?, ? extends DscResponse>> readingMap;
   protected final Class<C> cmdClass;

   private static synchronized void init() {
      if (readingMap == null) {
         readingMap = HashMultimap.create();
      }

   }

   public static DscCommandWithAppSeq tryToPrepare(ChannelHandlerContext context, Object object2) throws Exception {
      return tryToPrepare(Reading.class, context, object2);
   }

   public static void tryToParse(ChannelHandlerContext context, Object object2, List<Message.Response> responses) {
      if (object2 instanceof DscResponse) {
         DscResponse response2 = (DscResponse)object2;
         for (Reading<?, ?, ?> reading : readingMap.get(response2.getClass())) {
            try {
               reading.doParse(context, response2, responses);
            } catch (RuntimeException exception) {
               responses.add(DscError.newMessageError(reading, null, exception));
            }
         }
      }

   }

   Reading(Class<C> class2) {
      init();
      this.cmdClass = Preconditions.checkNotNull(class2);
      readingMap.put(class2, this);
   }

   protected final boolean expectedSuccessfulResponse() {
      return false;
   }

   @SuppressWarnings("unchecked")
   private void doParse(ChannelHandlerContext context, DscResponse response2, List<Message.Response> responses) {
      this.parseResponse(context, (C) response2, responses);
   }

   protected abstract void parseResponse(ChannelHandlerContext context, C c2, List<Message.Response> responses);
}
