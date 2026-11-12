package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.SystemCapabilities;

import java.util.List;

public class MaxUsersReading extends RequestableCommandReading<Void, Integer, SystemCapabilities> {
   public MaxUsersReading() {
      super(SystemCapabilities.class);
   }

   protected SystemCapabilities prepareRequest(ChannelHandlerContext context, Void void2) {
      return new SystemCapabilities();
   }

   protected void parseResponse(ChannelHandlerContext context, SystemCapabilities systemCapabilities2, List<Message.Response> responses) {
      responses.add(new NewValue(this, systemCapabilities2.getMaxUsers()));
   }
}
