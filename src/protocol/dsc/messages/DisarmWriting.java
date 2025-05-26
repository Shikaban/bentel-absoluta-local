package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.PartitionDisarmControl;

public class DisarmWriting extends Writing<Integer> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Integer var2) throws Exception {
      PartitionDisarmControl var3 = new PartitionDisarmControl();
      var3.setPartitionNumber(var2);
      var3.setUserCode(Messages.getPin(var1));
      return var3;
   }
}
