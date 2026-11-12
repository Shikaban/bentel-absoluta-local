package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.PartitionArmControl;

import org.javatuples.Pair;

public class ArmWriting extends Writing<Pair<Integer, Integer>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Pair<Integer, Integer> pair2) throws Exception {
      Integer partitionNumber = (Integer)pair2.getValue0();
      int armMode = (Integer)pair2.getValue1();
      PartitionArmControl partitionArmControl2 = new PartitionArmControl();
      partitionArmControl2.setPartitionNumber(partitionNumber);
      partitionArmControl2.setArmMode(armMode);
      partitionArmControl2.setUserCode(Messages.getPin(context));
      return partitionArmControl2;
   }
}
