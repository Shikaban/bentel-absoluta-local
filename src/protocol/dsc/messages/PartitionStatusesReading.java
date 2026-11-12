package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.PartitionStatus;

import java.util.Collections;
import java.util.List;

public class PartitionStatusesReading extends RequestableCommandReading<List<Integer>, List<List<Boolean>>, PartitionStatus> {
   public PartitionStatusesReading() {
      super(PartitionStatus.class);
   }

   protected PartitionStatus prepareRequest(ChannelHandlerContext context, List<Integer> items) {
      PartitionStatus partitionStatus2 = new PartitionStatus();
      partitionStatus2.setPartitions(items);
      return partitionStatus2;
   }

   protected void parseResponse(ChannelHandlerContext context, PartitionStatus partitionStatus2, List<Message.Response> responses) {
      List<Integer> partitions = partitionStatus2.getPartitions();
      List<? extends List<Boolean>> statuses = partitionStatus2.getStatuses();
      if (partitions.size() != statuses.size()) {
         throw new IllegalArgumentException("invalid partition status");
      } else {
         responses.add(new NewValue(this, partitions, Collections.unmodifiableList(statuses)));
      }
   }
}
