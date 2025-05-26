package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.CommandOutput;
import protocol.dsc.commands.DscCommandWithAppSeq;

import org.javatuples.Triplet;

public class OutputWriting extends Writing<Triplet<Integer, Integer, Integer>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Triplet<Integer, Integer, Integer> var2) throws Exception {
      Integer var3 = (Integer)var2.getValue0();
      Integer var4 = (Integer)var2.getValue1();
      int var5 = (Integer)var2.getValue2();
      CommandOutput var6 = new CommandOutput();
      var6.setPartitionNumber(var3);
      var6.setOutputNumber(var4);
      var6.setActivationType(var5);
      var6.setUserCode(Messages.getPin(var1));
      return var6;
   }
}
