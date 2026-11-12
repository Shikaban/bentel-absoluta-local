package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.AccessCodeLengthNotification;

import java.util.List;

public class CodeLengthReading extends RequestableCommandReading<Void, Integer, AccessCodeLengthNotification> {
   public CodeLengthReading() {
      super(AccessCodeLengthNotification.class);
   }

   protected AccessCodeLengthNotification prepareRequest(ChannelHandlerContext context, Void void2) {
      return new AccessCodeLengthNotification();
   }

   protected void parseResponse(ChannelHandlerContext context, AccessCodeLengthNotification accessCodeLengthNotification2, List<Message.Response> responses) {
      responses.add(new NewValue(this, accessCodeLengthNotification2.getLength()));
   }
}
