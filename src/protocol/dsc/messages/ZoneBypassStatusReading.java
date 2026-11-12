package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.ZoneBypassStatus;

import java.util.List;

public class ZoneBypassStatusReading extends RequestableCommandReading<Void, List<Integer>, ZoneBypassStatus> {
   public ZoneBypassStatusReading() {
      super(ZoneBypassStatus.class);
   }

   protected ZoneBypassStatus prepareRequest(ChannelHandlerContext context, Void void2) {
      ZoneBypassStatus zoneBypassStatus2 = new ZoneBypassStatus();
      zoneBypassStatus2.setGlobalPartition();
      return zoneBypassStatus2;
   }

   protected void parseResponse(ChannelHandlerContext context, ZoneBypassStatus zoneBypassStatus2, List<Message.Response> responses) {
      if (zoneBypassStatus2.getPartitionNumber() == 0) {
         List<Integer> bypassedZones = zoneBypassStatus2.getBypassedZones();
         responses.add(new NewValue(this, bypassedZones));
      } else {
         responses.add(DscError.newMessageError(this, null, (Integer)null, "unexpected zone bypass status for specific partition " + zoneBypassStatus2.getPartitionNumber()));
      }

   }
}
