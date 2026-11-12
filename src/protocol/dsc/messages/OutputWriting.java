package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.CommandOutput;
import protocol.dsc.commands.DscCommandWithAppSeq;

import org.javatuples.Triplet;

public class OutputWriting extends Writing<Triplet<Integer, Integer, Integer>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Triplet<Integer, Integer, Integer> triplet2) throws Exception {
      Integer partitionNumber = (Integer)triplet2.getValue0();
      Integer outputNumber = (Integer)triplet2.getValue1();
      int activationType = (Integer)triplet2.getValue2();
      CommandOutput commandOutput2 = new CommandOutput();
      commandOutput2.setPartitionNumber(partitionNumber);
      commandOutput2.setOutputNumber(outputNumber);
      commandOutput2.setActivationType(activationType);
      commandOutput2.setUserCode(Messages.getPin(context));
      return commandOutput2;
   }
}
