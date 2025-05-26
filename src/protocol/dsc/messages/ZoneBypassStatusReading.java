package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.ZoneBypassStatus;

import java.util.List;

public class ZoneBypassStatusReading extends RequestableCommandReading<Void, List<Integer>, ZoneBypassStatus> {
   public ZoneBypassStatusReading() {
      super(ZoneBypassStatus.class);
   }

   protected ZoneBypassStatus prepareRequest(ChannelHandlerContext var1, Void var2) {
      ZoneBypassStatus var3 = new ZoneBypassStatus();
      var3.setGlobalPartition();
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, ZoneBypassStatus var2, List<Message.Response> var3) {
      if (var2.getPartitionNumber() == 0) {
         List<Integer> var4 = var2.getBypassedZones();
         var3.add(new NewValue(this, var4));
      } else {
         var3.add(DscError.newMessageError(this, null, (Integer)null, "unexpected zone bypass status for specific partition " + var2.getPartitionNumber()));
      }

   }
}
