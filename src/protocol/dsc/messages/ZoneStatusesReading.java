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

   protected ZoneStatus prepareRequest(ChannelHandlerContext context, Pair<Integer, Integer> pair2) {
      ZoneStatus zoneStatus2 = new ZoneStatus();
      zoneStatus2.setZoneNumber((Integer)pair2.getValue0());
      zoneStatus2.setNumberOfZones((Integer)pair2.getValue1());
      return zoneStatus2;
   }

   protected void parseResponse(ChannelHandlerContext context, ZoneStatus zoneStatus2, List<Message.Response> responses) {
      int zoneNumber = zoneStatus2.getZoneNumber();
      int numberOfZones = zoneStatus2.getNumberOfZones();
      List<? extends List<Boolean>> zoneStatuses = zoneStatus2.getZoneStatuses();
      if (numberOfZones != zoneStatuses.size()) {
         throw new IllegalArgumentException("invalid zone status");
      } else {
         responses.add(new NewValue(this, Pair.with(zoneNumber, numberOfZones), Collections.unmodifiableList(zoneStatuses)));
      }
   }
}
