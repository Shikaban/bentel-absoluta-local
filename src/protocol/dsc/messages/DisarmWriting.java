package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.PartitionDisarmControl;

public class DisarmWriting extends Writing<Integer> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Integer partitionNumber) throws Exception {
      PartitionDisarmControl partitionDisarmControl2 = new PartitionDisarmControl();
      partitionDisarmControl2.setPartitionNumber(partitionNumber);
      partitionDisarmControl2.setUserCode(Messages.getPin(context));
      return partitionDisarmControl2;
   }
}
