package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.EnterAccessLevel;

import org.javatuples.Pair;

public class EnterAccessLevelWriting extends Writing<Pair<Integer, String>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Pair<Integer, String> var2) throws Exception {
      EnterAccessLevel var3 = new EnterAccessLevel();
      var3.setPartitionNumber((Integer)null);
      var3.setType((Integer)var2.getValue0());
      var3.setProgrammingAccessCode((String)var2.getValue1());
      return var3;
   }
}
