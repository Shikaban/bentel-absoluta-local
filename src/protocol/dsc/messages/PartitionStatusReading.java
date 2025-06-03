package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.PartitionStatus;

import java.util.Collections;
import java.util.List;

public class PartitionStatusReading extends RequestableCommandReading<Integer, List<Boolean>, PartitionStatus> {
   public PartitionStatusReading() {
      super(PartitionStatus.class);
   }

   @Override
   protected PartitionStatus prepareRequest(ChannelHandlerContext ctx, Integer partitionNumber) {
      PartitionStatus request = new PartitionStatus();
      request.setPartition(partitionNumber);
      return request;
   }

   @Override
   protected void parseResponse(ChannelHandlerContext ctx, PartitionStatus response, List<Message.Response> responses) {
      List<Integer> partitions = response.getPartitions();
      List<? extends List<Boolean>> statuses = response.getStatuses();
      if (partitions.size() != statuses.size()) {
         throw new IllegalArgumentException("Invalid partition status: partitions and statuses size mismatch");
      }
      for (int i = 0; i < partitions.size(); i++) {
         List<Boolean> status = Collections.unmodifiableList(statuses.get(i));
         responses.add(new NewValue(this, partitions.get(i), status));
      }
   }
}
