package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.EnterAccessLevel;

import org.javatuples.Pair;

public class EnterAccessLevelWriting extends Writing<Pair<Integer, String>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Pair<Integer, String> pair2) throws Exception {
      EnterAccessLevel enterAccessLevel2 = new EnterAccessLevel();
      enterAccessLevel2.setPartitionNumber((Integer)null);
      enterAccessLevel2.setType((Integer)pair2.getValue0());
      enterAccessLevel2.setProgrammingAccessCode((String)pair2.getValue1());
      return enterAccessLevel2;
   }
}
