package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.TestCommand;

import org.javatuples.Pair;

public class TestCommandWriting extends Writing<Pair<Integer, String>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Pair<Integer, String> var2) {
      Integer var3 = (Integer)var2.getValue0();
      String var4 = (String)var2.getValue1();
      TestCommand var5 = new TestCommand(var3);
      var5.setPayload(var4);
      return var5;
   }
}
