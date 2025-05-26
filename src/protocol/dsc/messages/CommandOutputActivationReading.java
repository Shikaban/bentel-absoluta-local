package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.CommandOutputActivation;

import java.util.List;

public class CommandOutputActivationReading extends RequestableCommandReading<Void, List<Integer>, CommandOutputActivation> {
   public CommandOutputActivationReading() {
      super(CommandOutputActivation.class);
   }

   protected CommandOutputActivation prepareRequest(ChannelHandlerContext var1, Void var2) throws Exception {
      CommandOutputActivation var3 = new CommandOutputActivation();
      var3.setPartitionNumber((Integer)null);
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, CommandOutputActivation var2, List<Message.Response> var3) {
      var3.add(new NewValue(this, null, var2.getActiveOutputs()));
   }
}
