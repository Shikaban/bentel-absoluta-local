package protocol.dsc.messages;

import com.google.common.base.Preconditions;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.CommandRequest;
import protocol.dsc.commands.DscRequestableCommand;

abstract class RequestableCommandReading<P, V, C extends DscRequestableCommand> extends Reading<P, V, C> {
   RequestableCommandReading(Class<C> class2) {
      super(class2);
   }

   protected final CommandRequest prepareCommand(ChannelHandlerContext context, P p2) throws Exception {
      DscRequestableCommand requestableCommand = (DscRequestableCommand)Preconditions.checkNotNull(this.prepareRequest(context, p2));
      CommandRequest commandRequest2 = new CommandRequest();
      commandRequest2.setRequestedCmd(requestableCommand);
      return commandRequest2;
   }

   protected abstract C prepareRequest(ChannelHandlerContext context, P p2) throws Exception;
}
