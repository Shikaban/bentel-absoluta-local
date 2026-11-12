package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.ZoneAssignmentConfiguration;

import java.util.List;

public class ZoneAssignmentReading extends RequestableCommandReading<Integer, List<Integer>, ZoneAssignmentConfiguration> {
   public ZoneAssignmentReading() {
      super(ZoneAssignmentConfiguration.class);
   }

   protected ZoneAssignmentConfiguration prepareRequest(ChannelHandlerContext context, Integer partitionNumber) throws Exception {
      ZoneAssignmentConfiguration zoneAssignmentConfiguration2 = new ZoneAssignmentConfiguration();
      zoneAssignmentConfiguration2.setPartitionNumber(partitionNumber);
      return zoneAssignmentConfiguration2;
   }

   protected void parseResponse(ChannelHandlerContext context, ZoneAssignmentConfiguration zoneAssignmentConfiguration2, List<Message.Response> responses) {
      responses.add(new NewValue(this, zoneAssignmentConfiguration2.getPartitionNumber(), zoneAssignmentConfiguration2.getAssignedZones()));
   }
}
