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

import java.util.logging.Logger;

public class PollHandler extends ChannelInboundHandlerAdapter {
   private static final Logger logger = Logger.getLogger(PollHandler.class.getName());
   private static final AttributeKey<Boolean> POLL_KEY = AttributeKey.valueOf("PollHandler.poll");
   private final PollHandler.PollFactory pollFactory;

   public PollHandler(PollHandler.PollFactory pollFactory2) {
      this.pollFactory = (PollHandler.PollFactory)Preconditions.checkNotNull(pollFactory2);
   }

   public void userEventTriggered(ChannelHandlerContext context, Object object2) throws Exception {
      if (object2 instanceof IdleStateEvent && ((IdleStateEvent)object2).state() == IdleState.WRITER_IDLE) {
         if (isPollEnabled(context.channel())) {
            logger.finer("Sending poll");
            DscCommand command = this.pollFactory.createPoll();
            context.write(command).addListener(LogOnFailure.INSTANCE);
         }
      } else {
         super.userEventTriggered(context, object2);
      }

   }

   public static boolean isPollEnabled(Channel channel2) {
      return Boolean.TRUE.equals(channel2.attr(POLL_KEY).get());
   }

   public static void setPollEnabled(Channel channel2, boolean enabled) {
      channel2.attr(POLL_KEY).set(enabled);
   }

   public interface PollFactory {
      DscCommand createPoll();
   }
}
