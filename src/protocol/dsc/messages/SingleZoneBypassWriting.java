package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.SingleZoneBypassWrite;

import org.javatuples.Triplet;

public class SingleZoneBypassWriting extends Writing<Triplet<Integer, Integer, Boolean>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Triplet<Integer, Integer, Boolean> triplet2) throws Exception {
      Integer partitionNumber = (Integer)triplet2.getValue0();
      int zoneNumber = (Integer)triplet2.getValue1();
      boolean enabled = (Boolean)triplet2.getValue2();
      SingleZoneBypassWrite singleZoneBypassWrite2 = new SingleZoneBypassWrite();
      singleZoneBypassWrite2.setPartitionNumber(partitionNumber);
      singleZoneBypassWrite2.setZoneNumber(zoneNumber);
      singleZoneBypassWrite2.setBypass(enabled);
      return singleZoneBypassWrite2;
   }
}
