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
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class PipelineHandler extends ChannelInboundHandlerAdapter {
   private final Queue<HandshakeHandler<?>> handshakeHandlers;
   private final List<ChannelHandler> normalModeHandlers;
   private final List<String> managedHandlerNames = new ArrayList();
   private static final boolean VERBOSE_DEBUG = false;

   public PipelineHandler(Queue<HandshakeHandler<?>> var1, List<ChannelHandler> var2) {
      this.handshakeHandlers = (Queue)Preconditions.checkNotNull(var1);
      this.normalModeHandlers = (List)Preconditions.checkNotNull(var2);
   }

   public void handlerAdded(ChannelHandlerContext var1) throws Exception {
      SessionInfo var2 = SessionInfo.getOwnInfo(var1.channel());
      if (var2.getClient() == null) {
         throw new IllegalStateException("invalid own info (null client)");
      } else {
         Iterator var3 = this.handshakeHandlers.iterator();

         HandshakeHandler var4;
         do {
            if (!var3.hasNext()) {
               return;
            }

            var4 = (HandshakeHandler)var3.next();
         } while(var4.validateOwnInfo(var2));

         throw new IllegalStateException(String.format("invalid own info (reported by %s)", var4.getClass().getSimpleName()));
      }
   }

   public void channelActive(ChannelHandlerContext var1) throws Exception {
      super.channelActive(var1);
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: handshake begin");
      }
      var1.fireUserEventTriggered(SimpleMessage.HANDSHAKE_BEGIN_EVENT);
      this.nextStage(var1);
   }

   public void handlerRemoved(ChannelHandlerContext var1) throws Exception {
      this.setManagedHandlers(var1, Collections.emptyList());
      super.handlerRemoved(var1);
   }

   public void userEventTriggered(ChannelHandlerContext var1, Object var2) throws Exception {
      if (var2 == SimpleMessage.HANDSHAKE_STAGE_COMPLETED_EVENT) {
         this.nextStage(var1);
      } else {
         super.userEventTriggered(var1, var2);
      }

   }

   private void nextStage(ChannelHandlerContext var1) {
      HandshakeHandler<?> var2 = (HandshakeHandler)this.handshakeHandlers.poll();
      if (var2 != null) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: handshake next stage");
         }
         this.setManagedHandlers(var1, Collections.singletonList(var2));
         SessionInfo var3 = SessionInfo.getOwnInfo(var1.channel());
         if (var3.isClient()) {
            var2.startHandshakeStage();
         }
      } else {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: handshake end");
         }
         this.setManagedHandlers(var1, this.normalModeHandlers);
         var1.fireUserEventTriggered(SimpleMessage.HANDSHAKE_END_EVENT);
      }
   }

   private void setManagedHandlers(ChannelHandlerContext var1, List<ChannelHandler> var2) {
      ChannelPipeline var3 = var1.pipeline();
      Iterator var4 = this.managedHandlerNames.iterator();

      String var5;
      while(var4.hasNext()) {
         var5 = (String)var4.next();
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: removing " + var5 + " from pipeline");
         }
         var3.remove(var5);
      }

      this.managedHandlerNames.clear();
      String var9 = var1.name();
      var5 = var9;

      for(int var6 = var2.size() - 1; var6 >= 0; --var6) {
         ChannelHandler var7 = (ChannelHandler)var2.get(var6);
         String var8 = String.format("%s:%s#%d", var9, var7.getClass().getSimpleName(), var6);
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: adding " + var8 + " to pipeline before " + var5);
         }
         var3.addBefore(var5, var8, var7);
         this.managedHandlerNames.add(var8);
         var5 = var8;
      }
   }
}
