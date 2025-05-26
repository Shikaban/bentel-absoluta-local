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

   protected SystemCapabilities prepareRequest(ChannelHandlerContext var1, Void var2) {
      return new SystemCapabilities();
   }

   protected void parseResponse(ChannelHandlerContext var1, SystemCapabilities var2, List<Message.Response> var3) {
      var3.add(new NewValue(this, var2.getMaxUsers()));
   }
}
