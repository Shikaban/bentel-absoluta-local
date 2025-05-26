package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.ExitConfigurationMode;

public class ExitConfigurationModeWriting extends Writing<Integer> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Integer var2) throws Exception {
      ExitConfigurationMode var3 = new ExitConfigurationMode();
      var3.setPartitionNumber(var2);
      return var3;
   }
}
