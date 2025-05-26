package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.ZoneStatus;

import java.util.Collections;
import java.util.List;
import org.javatuples.Pair;

public class ZoneStatusesReading extends RequestableCommandReading<Pair<Integer, Integer>, List<List<Boolean>>, ZoneStatus> {
   public ZoneStatusesReading() {
      super(ZoneStatus.class);
   }

   protected ZoneStatus prepareRequest(ChannelHandlerContext var1, Pair<Integer, Integer> var2) {
      ZoneStatus var3 = new ZoneStatus();
      var3.setZoneNumber((Integer)var2.getValue0());
      var3.setNumberOfZones((Integer)var2.getValue1());
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, ZoneStatus var2, List<Message.Response> var3) {
      int var4 = var2.getZoneNumber();
      int var5 = var2.getNumberOfZones();
      List<? extends List<Boolean>> var6 = var2.getZoneStatuses();
      if (var5 != var6.size()) {
         throw new IllegalArgumentException("invalid zone status");
      } else {
         var3.add(new NewValue(this, Pair.with(var4, var5), Collections.unmodifiableList(var6)));
      }
   }
}
