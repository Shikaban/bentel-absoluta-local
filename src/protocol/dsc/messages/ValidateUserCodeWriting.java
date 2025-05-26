package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.UserActivity;

import org.javatuples.Pair;

public class ValidateUserCodeWriting extends Writing<Pair<Integer, String>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Pair<Integer, String> var2) throws Exception {
      UserActivity var3 = new UserActivity();
      var3.setPartitionNumber((Integer)var2.getValue0());
      var3.setType(22);
      var3.setUserCode((String)var2.getValue1());
      return var3;
   }
}
