package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.ExitConfigurationMode;

public class ExitConfigurationModeWriting extends Writing<Integer> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Integer partitionNumber) throws Exception {
      ExitConfigurationMode exitConfigurationMode2 = new ExitConfigurationMode();
      exitConfigurationMode2.setPartitionNumber(partitionNumber);
      return exitConfigurationMode2;
   }
}
