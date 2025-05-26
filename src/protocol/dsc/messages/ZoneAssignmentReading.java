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

   protected ZoneAssignmentConfiguration prepareRequest(ChannelHandlerContext var1, Integer var2) throws Exception {
      ZoneAssignmentConfiguration var3 = new ZoneAssignmentConfiguration();
      var3.setPartitionNumber(var2);
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, ZoneAssignmentConfiguration var2, List<Message.Response> var3) {
      var3.add(new NewValue(this, var2.getPartitionNumber(), var2.getAssignedZones()));
   }
}
