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

   protected void parseResponse(ChannelHandlerContext var1, PartitionAssignmentsResponse var2, List<Message.Response> var3) {
      int var4 = var2.getUserNumberStart();
      for (List<Integer> var6 : var2.getPartitionAssignments()) {
         var3.add(new NewValue(this, var4++, var6));
      }

   }

   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Integer var2) throws Exception {
      PartitionAssignments var3 = new PartitionAssignments();
      var3.setUserNumberStart(var2);
      var3.setNumberOfUsers(1);
      return var3;
   }
}
