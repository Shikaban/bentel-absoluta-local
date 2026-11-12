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

   protected CommandOutputActivation prepareRequest(ChannelHandlerContext context, Void void2) throws Exception {
      CommandOutputActivation commandOutputActivation2 = new CommandOutputActivation();
      commandOutputActivation2.setPartitionNumber((Integer)null);
      return commandOutputActivation2;
   }

   protected void parseResponse(ChannelHandlerContext context, CommandOutputActivation commandOutputActivation2, List<Message.Response> responses) {
      responses.add(new NewValue(this, null, commandOutputActivation2.getActiveOutputs()));
   }
}
