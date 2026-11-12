package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.UserActivity;

public class UserActivityWriting extends Writing<Integer> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Integer type) throws Exception {
      UserActivity userActivity2 = new UserActivity();
      userActivity2.setPartitionNumber((Integer)null);
      userActivity2.setType(type);
      return userActivity2;
   }
}
