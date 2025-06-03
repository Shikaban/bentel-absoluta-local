package protocol.dsc.transport;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import protocol.dsc.session.SessionInfo;
import protocol.dsc.transport.command_handlers.HandshakeHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class PipelineHandler extends ChannelInboundHandlerAdapter {
   private final Queue<HandshakeHandler<?>> handshakeHandlers;
   private final List<ChannelHandler> normalModeHandlers;
   private final List<String> managedHandlerNames = new ArrayList<>();
   private static final boolean VERBOSE_DEBUG = false;

   public PipelineHandler(Queue<HandshakeHandler<?>> handshakeHandlers, List<ChannelHandler> normalModeHandlers) {
      this.handshakeHandlers = Preconditions.checkNotNull(handshakeHandlers);
      this.normalModeHandlers = Preconditions.checkNotNull(normalModeHandlers);
   }

   @Override
   public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
      SessionInfo sessionInfo = SessionInfo.getOwnInfo(ctx.channel());
      if (sessionInfo.getClient() == null) {
         throw new IllegalStateException("invalid own info (null client)");
      }
      for (HandshakeHandler<?> handler : this.handshakeHandlers) {
         if (!handler.validateOwnInfo(sessionInfo)) {
               throw new IllegalStateException(
                  String.format("invalid own info (reported by %s)", handler.getClass().getSimpleName())
               );
         }
      }
   }

   @Override
   public void channelActive(ChannelHandlerContext ctx) throws Exception {
      super.channelActive(ctx);
      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: handshake begin");
      }
      ctx.fireUserEventTriggered(SimpleMessage.HANDSHAKE_BEGIN_EVENT);
      this.nextStage(ctx);
   }

   @Override
   public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      this.setManagedHandlers(ctx, Collections.emptyList());
      super.handlerRemoved(ctx);
   }

   @Override
   public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt == SimpleMessage.HANDSHAKE_STAGE_COMPLETED_EVENT) {
         this.nextStage(ctx);
      } else {
         super.userEventTriggered(ctx, evt);
      }
   }

   private void nextStage(ChannelHandlerContext ctx) {
      HandshakeHandler<?> handler = this.handshakeHandlers.poll();
      if (handler != null) {
         if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: handshake next stage");
         }
         this.setManagedHandlers(ctx, Collections.singletonList(handler));
         SessionInfo sessionInfo = SessionInfo.getOwnInfo(ctx.channel());
         if (sessionInfo.isClient()) {
               handler.startHandshakeStage();
         }
      } else {
         if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: handshake end");
         }
         this.setManagedHandlers(ctx, this.normalModeHandlers);
         ctx.fireUserEventTriggered(SimpleMessage.HANDSHAKE_END_EVENT);
      }
   }

   private void setManagedHandlers(ChannelHandlerContext ctx, List<ChannelHandler> handlers) {
      ChannelPipeline pipeline = ctx.pipeline();

      // Remove all managed handlers
      for (String handlerName : this.managedHandlerNames) {
         if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: removing " + handlerName + " from pipeline");
         }
         pipeline.remove(handlerName);
      }
      this.managedHandlerNames.clear();

      String previousName = ctx.name();

      // Add new handlers in reverse order
      for (int i = handlers.size() - 1; i >= 0; --i) {
         ChannelHandler handler = handlers.get(i);
         String handlerName = String.format("%s:%s#%d", ctx.name(), handler.getClass().getSimpleName(), i);
         if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: adding " + handlerName + " to pipeline before " + previousName);
         }
         pipeline.addBefore(previousName, handlerName, handler);
         this.managedHandlerNames.add(handlerName);
         previousName = handlerName;
      }
   }
}
