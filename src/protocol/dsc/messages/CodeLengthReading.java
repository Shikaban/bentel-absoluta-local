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

   protected AccessCodeLengthNotification prepareRequest(ChannelHandlerContext var1, Void var2) {
      return new AccessCodeLengthNotification();
   }

   protected void parseResponse(ChannelHandlerContext var1, AccessCodeLengthNotification var2, List<Message.Response> var3) {
      var3.add(new NewValue(this, var2.getLength()));
   }
}
