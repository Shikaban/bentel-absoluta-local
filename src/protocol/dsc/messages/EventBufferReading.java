package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.EventBufferRead;
import protocol.dsc.commands.EventBufferReadResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.javatuples.Pair;
import org.javatuples.Septet;

public class EventBufferReading extends Reading<Pair<Integer, Integer>, List<Septet<Calendar, Integer, Boolean, Integer, Integer, Integer, List<Integer>>>, EventBufferReadResponse> {

   public EventBufferReading() {
      super(EventBufferReadResponse.class);
   }

   protected void parseResponse(ChannelHandlerContext var1, EventBufferReadResponse var2, List<Message.Response> var3) {
      if (var2.getBufferID() != 3) {
         System.out.println("WARN: unmanaged buffer ID: " + var2.getBufferID());
      } else {
         List<EventBufferReadResponse.Event> var4 = var2.getEvents();

         assert var4.size() == var2.getNumberOfEvents();

         Pair<Integer, Integer> var5 = Pair.with(var2.getEventNumber(), var2.getNumberOfEvents());
         List<Septet<Calendar, Integer, Boolean, Integer, Integer, Integer, List<Integer>>> var6 = new ArrayList(var4.size());

         EventBufferReadResponse.Event var8;
         List var10;
         for(Iterator var7 = var4.iterator(); var7.hasNext(); var6.add(Septet.with(var8.getDateTimeStamp(), var8.getEventClass(), var8.isRestore(), var8.getEventCode(), var8.getWhereWhy(), var8.getWho(), var10))) {
            var8 = (EventBufferReadResponse.Event)var7.next();
            if (var8.getFlags() != 9) {
               System.out.println("WARN: unexpected flags: " + var8.getFlags());
               return;
            }

            int var9 = var8.getPartitionMaskUnusedBytes();
            var10 = null;
            if (var9 == 0) {
               var10 = var8.getPartitions();
            } else if (var9 != 65535) {
               System.out.println("WARN: unexpected partition mask unused bytes: " + var9);
            }
         }

         var3.add(new NewValue(this, var5, var6));
      }
   }

   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Pair<Integer, Integer> var2) throws Exception {
      EventBufferRead var3 = new EventBufferRead();
      var3.setBufferID(3);
      var3.setEventNumber((Integer)var2.getValue0());
      var3.setNumberOfEvents((Integer)var2.getValue1());
      return var3;
   }
}
