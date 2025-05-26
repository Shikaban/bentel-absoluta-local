package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.SingleZoneBypassStatus;

import java.util.List;

public class SingleZoneBypassStatusReading extends RequestableCommandReading<Integer, Boolean, SingleZoneBypassStatus> {
   public SingleZoneBypassStatusReading() {
      super(SingleZoneBypassStatus.class);
   }

   protected SingleZoneBypassStatus prepareRequest(ChannelHandlerContext var1, Integer var2) {
      SingleZoneBypassStatus var3 = new SingleZoneBypassStatus();
      var3.setZoneNumber(var2);
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, SingleZoneBypassStatus var2, List<Message.Response> var3) {
      int var4 = var2.getZoneNumber();
      boolean var5 = var2.isBypassed();
      var3.add(new NewValue(this, var4, var5));
   }
}
