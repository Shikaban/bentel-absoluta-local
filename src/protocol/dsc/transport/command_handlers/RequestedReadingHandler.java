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

   public void write(ChannelHandlerContext var1, Object var2, ChannelPromise var3) throws Exception {
      DscCommandWithAppSeq var4 = Reading.tryToPrepare(var1, var2);
      if (var4 != null) {
         var1.write(var4, var3);
      } else {
         super.write(var1, var2, var3);
      }

   }

   public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
      try {
         Reading.tryToParse(var1, var2, this.out);
         if (!this.out.isEmpty()) {
            for (Message.Response response : this.out) {
               var1.fireChannelRead(response);
            }
         } else {
            super.channelRead(var1, var2);
         }
      } finally {
         this.out.clear();
      }

   }
}
