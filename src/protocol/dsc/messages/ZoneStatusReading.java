package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.base.DscBitMask;
import protocol.dsc.commands.ZoneStatus;

import java.util.List;

public class ZoneStatusReading extends RequestableCommandReading<Integer, List<Boolean>, ZoneStatus> {

   public ZoneStatusReading() {
      super(ZoneStatus.class);
   }

   @Override
   protected ZoneStatus prepareRequest(ChannelHandlerContext ctx, Integer zoneNumber) {
      ZoneStatus request = new ZoneStatus();
      request.setZoneNumber(zoneNumber);
      request.setNumberOfZones(1);
      return request;
   }

   @Override
   protected void parseResponse(ChannelHandlerContext ctx, ZoneStatus response, List<Message.Response> out) {
      int startZone = response.getZoneNumber();
      int zoneCount = response.getNumberOfZones();
      List<DscBitMask> zoneStatuses = response.getZoneStatuses();

      for (int i = 0; i < zoneCount; i++) {
         DscBitMask bitMask = zoneStatuses.get(i);
         List<Boolean> statusList = bitMask;
         out.add(new NewValue(this, startZone + i, statusList));
      }
   }
}
