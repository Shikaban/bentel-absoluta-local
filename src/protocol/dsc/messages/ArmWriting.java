package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.PartitionArmControl;

import org.javatuples.Pair;

public class ArmWriting extends Writing<Pair<Integer, Integer>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Pair<Integer, Integer> var2) throws Exception {
      Integer var3 = (Integer)var2.getValue0();
      int var4 = (Integer)var2.getValue1();
      PartitionArmControl var5 = new PartitionArmControl();
      var5.setPartitionNumber(var3);
      var5.setArmMode(var4);
      var5.setUserCode(Messages.getPin(var1));
      return var5;
   }
}
