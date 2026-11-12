package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.TestCommand;

import org.javatuples.Pair;

public class TestCommandWriting extends Writing<Pair<Integer, String>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Pair<Integer, String> pair2) {
      Integer commandNumber = (Integer)pair2.getValue0();
      String text = (String)pair2.getValue1();
      TestCommand testCommand2 = new TestCommand(commandNumber);
      testCommand2.setPayload(text);
      return testCommand2;
   }
}
