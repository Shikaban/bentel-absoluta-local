package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.PartitionAssignmentConfiguration;

import java.util.List;

public class PartitionAssignmentConfigurationReading extends RequestableCommandReading<Void, List<Integer>, PartitionAssignmentConfiguration> {
   public PartitionAssignmentConfigurationReading() {
      super(PartitionAssignmentConfiguration.class);
   }

   protected PartitionAssignmentConfiguration prepareRequest(ChannelHandlerContext var1, Void var2) {
      return new PartitionAssignmentConfiguration();
   }

   protected void parseResponse(ChannelHandlerContext var1, PartitionAssignmentConfiguration var2, List<Message.Response> var3) {
      var3.add(new NewValue(this, var2.getAssignedPartitions()));
   }
}
