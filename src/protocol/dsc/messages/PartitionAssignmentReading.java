package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.PartitionAssignments;
import protocol.dsc.commands.PartitionAssignmentsResponse;

import java.util.List;

public class PartitionAssignmentReading extends Reading<Integer, List<Integer>, PartitionAssignmentsResponse> {
   public PartitionAssignmentReading() {
      super(PartitionAssignmentsResponse.class);
   }

   protected void parseResponse(ChannelHandlerContext context, PartitionAssignmentsResponse partitionAssignmentsResponse2, List<Message.Response> responses) {
      int userNumberStart = partitionAssignmentsResponse2.getUserNumberStart();
      for (List<Integer> items : partitionAssignmentsResponse2.getPartitionAssignments()) {
         responses.add(new NewValue(this, userNumberStart++, items));
      }

   }

   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Integer userNumberStart) throws Exception {
      PartitionAssignments partitionAssignments2 = new PartitionAssignments();
      partitionAssignments2.setUserNumberStart(userNumberStart);
      partitionAssignments2.setNumberOfUsers(1);
      return partitionAssignments2;
   }
}
