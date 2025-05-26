package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.SingleZoneBypassWrite;

import org.javatuples.Triplet;

public class SingleZoneBypassWriting extends Writing<Triplet<Integer, Integer, Boolean>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Triplet<Integer, Integer, Boolean> var2) throws Exception {
      Integer var3 = (Integer)var2.getValue0();
      int var4 = (Integer)var2.getValue1();
      boolean var5 = (Boolean)var2.getValue2();
      SingleZoneBypassWrite var6 = new SingleZoneBypassWrite();
      var6.setPartitionNumber(var3);
      var6.setZoneNumber(var4);
      var6.setBypass(var5);
      return var6;
   }
}
