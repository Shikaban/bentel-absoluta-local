package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.UserActivity;

public class UserActivityWriting extends Writing<Integer> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Integer var2) throws Exception {
      UserActivity var3 = new UserActivity();
      var3.setPartitionNumber((Integer)null);
      var3.setType(var2);
      return var3;
   }
}
