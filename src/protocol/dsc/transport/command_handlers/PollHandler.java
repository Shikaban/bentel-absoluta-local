package protocol.dsc.transport.command_handlers;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import protocol.dsc.commands.DscCommand;
import protocol.dsc.util.LogOnFailure;

public class PollHandler extends ChannelInboundHandlerAdapter {
   private static final AttributeKey<Boolean> POLL_KEY = AttributeKey.valueOf("PollHandler.poll");
   private final PollHandler.PollFactory pollFactory;

   public PollHandler(PollHandler.PollFactory var1) {
      this.pollFactory = (PollHandler.PollFactory)Preconditions.checkNotNull(var1);
   }

   public void userEventTriggered(ChannelHandlerContext var1, Object var2) throws Exception {
      if (var2 instanceof IdleStateEvent && ((IdleStateEvent)var2).state() == IdleState.WRITER_IDLE) {
         if (isPollEnabled(var1.channel())) {
            System.out.println("TRACE: sending poll");
            DscCommand var3 = this.pollFactory.createPoll();
            var1.write(var3).addListener(LogOnFailure.INSTANCE);
         }
      } else {
         super.userEventTriggered(var1, var2);
      }

   }

   public static boolean isPollEnabled(Channel var0) {
      return Boolean.TRUE.equals(var0.attr(POLL_KEY).get());
   }

   public static void setPollEnabled(Channel var0, boolean var1) {
      var0.attr(POLL_KEY).set(var1);
   }

   public interface PollFactory {
      DscCommand createPoll();
   }
}
