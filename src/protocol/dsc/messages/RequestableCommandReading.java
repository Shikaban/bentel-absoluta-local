package protocol.dsc.messages;

import com.google.common.base.Preconditions;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.CommandRequest;
import protocol.dsc.commands.DscRequestableCommand;

abstract class RequestableCommandReading<P, V, C extends DscRequestableCommand> extends Reading<P, V, C> {
   RequestableCommandReading(Class<C> var1) {
      super(var1);
   }

   protected final CommandRequest prepareCommand(ChannelHandlerContext var1, P var2) throws Exception {
      DscRequestableCommand var3 = (DscRequestableCommand)Preconditions.checkNotNull(this.prepareRequest(var1, var2));
      CommandRequest var4 = new CommandRequest();
      var4.setRequestedCmd(var3);
      return var4;
   }

   protected abstract C prepareRequest(ChannelHandlerContext var1, P var2) throws Exception;
}
