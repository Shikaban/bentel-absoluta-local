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

   protected SingleZoneBypassStatus prepareRequest(ChannelHandlerContext context, Integer zoneNumber) {
      SingleZoneBypassStatus singleZoneBypassStatus2 = new SingleZoneBypassStatus();
      singleZoneBypassStatus2.setZoneNumber(zoneNumber);
      return singleZoneBypassStatus2;
   }

   protected void parseResponse(ChannelHandlerContext context, SingleZoneBypassStatus singleZoneBypassStatus2, List<Message.Response> responses) {
      int zoneNumber = singleZoneBypassStatus2.getZoneNumber();
      boolean bypassed = singleZoneBypassStatus2.isBypassed();
      responses.add(new NewValue(this, zoneNumber, bypassed));
   }
}
