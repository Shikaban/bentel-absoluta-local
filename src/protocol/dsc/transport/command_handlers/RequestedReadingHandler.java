package protocol.dsc.transport.command_handlers;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import protocol.dsc.Message;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.messages.Reading;

import java.util.ArrayList;
import java.util.List;

public class RequestedReadingHandler extends ChannelDuplexHandler {
   private final List<Message.Response> out = new ArrayList<Message.Response>();

   public void write(ChannelHandlerContext context, Object object2, ChannelPromise promise) throws Exception {
      DscCommandWithAppSeq commandWithAppSeq = Reading.tryToPrepare(context, object2);
      if (commandWithAppSeq != null) {
         context.write(commandWithAppSeq, promise);
      } else {
         super.write(context, object2, promise);
      }

   }

   public void channelRead(ChannelHandlerContext context, Object object2) throws Exception {
      try {
         Reading.tryToParse(context, object2, this.out);
         if (!this.out.isEmpty()) {
            for (Message.Response response : this.out) {
               context.fireChannelRead(response);
            }
         } else {
            super.channelRead(context, object2);
         }
      } finally {
         this.out.clear();
      }

   }
}
