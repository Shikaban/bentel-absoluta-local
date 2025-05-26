package protocol.dsc.messages;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.DscResponse;

import java.util.Iterator;
import java.util.List;

public abstract class Reading<P, V, C extends DscResponse> extends MessageWithResponse<P, V> {
   private static Multimap<Class<? extends DscResponse>, Reading<?, ?, ? extends DscResponse>> readingMap;
   protected final Class<C> cmdClass;

   private static synchronized void init() {
      if (readingMap == null) {
         readingMap = HashMultimap.create();
      }

   }

   public static DscCommandWithAppSeq tryToPrepare(ChannelHandlerContext var0, Object var1) throws Exception {
      return tryToPrepare(Reading.class, var0, var1);
   }

   public static void tryToParse(ChannelHandlerContext var0, Object var1, List<Message.Response> var2) {
      if (var1 instanceof DscResponse) {
         DscResponse var3 = (DscResponse)var1;
         Iterator var4 = readingMap.get(var3.getClass()).iterator();

         while(var4.hasNext()) {
            Reading var5 = (Reading)var4.next();

            try {
               var5.doParse(var0, var3, var2);
            } catch (RuntimeException var7) {
               var2.add(DscError.newMessageError(var5, (Object)null, var7));
            }
         }
      }

   }

   Reading(Class<C> var1) {
      init();
      this.cmdClass = (Class)Preconditions.checkNotNull(var1);
      readingMap.put(var1, this);
   }

   protected final boolean expectedSuccessfulResponse() {
      return false;
   }

   private void doParse(ChannelHandlerContext var1, DscResponse var2, List<Message.Response> var3) {
      this.parseResponse(var1, (C) var2, var3);
   }

   protected abstract void parseResponse(ChannelHandlerContext var1, C var2, List<Message.Response> var3);
}
