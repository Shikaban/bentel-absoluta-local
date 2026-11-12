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

   protected PartitionAssignmentConfiguration prepareRequest(ChannelHandlerContext context, Void void2) {
      return new PartitionAssignmentConfiguration();
   }

   protected void parseResponse(ChannelHandlerContext context, PartitionAssignmentConfiguration partitionAssignmentConfiguration2, List<Message.Response> responses) {
      responses.add(new NewValue(this, partitionAssignmentConfiguration2.getAssignedPartitions()));
   }
}
