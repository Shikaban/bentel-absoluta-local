package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.UserActivity;

import org.javatuples.Pair;

public class ValidateUserCodeWriting extends Writing<Pair<Integer, String>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Pair<Integer, String> pair2) throws Exception {
      UserActivity userActivity2 = new UserActivity();
      userActivity2.setPartitionNumber((Integer)pair2.getValue0());
      userActivity2.setType(22);
      userActivity2.setUserCode((String)pair2.getValue1());
      return userActivity2;
   }
}
