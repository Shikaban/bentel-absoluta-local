package protocol.dsc.transport.command_handlers;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import protocol.dsc.commands.CommandResponse;
import protocol.dsc.commands.DscCommand;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.DscGeneralResponse;
import protocol.dsc.commands.EndSession;
import protocol.dsc.session.SessionInfo;
import protocol.dsc.transport.SimpleMessage;
import protocol.dsc.util.LogOnFailure;

public abstract class HandshakeHandler<C extends DscCommand> extends ChannelInboundHandlerAdapter {
   private final Class<C> commandClass;
   private ChannelHandlerContext context;
   private boolean commandSent;
   private boolean commandReceived;
   private static final boolean VERBOSE_DEBUG = false;

   protected HandshakeHandler(Class<C> commandClass) {
      if (this.isSharable()) {
         throw new IllegalStateException("@Sharable annotation is not allowed");
      }
      this.commandClass = Preconditions.checkNotNull(commandClass);
   }

   public abstract boolean validateOwnInfo(SessionInfo sessionInfo);

   protected abstract C getCommand(Channel channel);

   protected int onCommandReceived(Channel channel, C command) {
      return 0;
   }

   protected void onCommandSent(Channel channel) {
   }

   public void startHandshakeStage() {
      if (this.context == null) {
         throw new IllegalStateException("No context available");
      }
      sendCommand(this.context);
   }

   @Override
   public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
      this.context = ctx;
      super.handlerAdded(ctx);
   }

   @Override
   public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      this.context = null;
      super.handlerRemoved(ctx);
   }

   @Override
   public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (commandClass.isInstance(msg)) {
         this.commandReceived = true;
         C command = commandClass.cast(msg);
         int responseCode = this.onCommandReceived(ctx.channel(), command);

         if (command instanceof DscCommandWithAppSeq) {
               DscCommandWithAppSeq withSeq = (DscCommandWithAppSeq) command;
               CommandResponse response = new CommandResponse();
               response.setCommandSeq(withSeq.getAppSeq());
               response.setResponseCode(responseCode);
               ctx.write(response).addListener(LogOnFailure.INSTANCE);
         }

         if (responseCode == 0) {
               onReceptionSuccess(ctx);
         } else {
               onFailure(ctx);
         }
      } else {
         super.channelRead(ctx, msg);
      }
   }

   private void onSendSuccess(ChannelHandlerContext ctx) {
      this.commandSent = true;
      if (this.commandReceived) {
         onSuccess(ctx);
      }
   }

   private void onReceptionSuccess(ChannelHandlerContext ctx) {
      this.commandReceived = true;
      if (this.commandSent) {
         onSuccess(ctx);
      } else {
         sendCommand(ctx);
      }
   }

   private void onSuccess(ChannelHandlerContext ctx) {
      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: handshake stage completed for " + this.commandClass.getSimpleName());
      }
      ctx.fireUserEventTriggered(SimpleMessage.HANDSHAKE_STAGE_COMPLETED_EVENT);
   }

   private void onFailure(ChannelHandlerContext ctx) {
      ctx.write(new EndSession()).addListener(LogOnFailure.INSTANCE);
   }

   private void sendCommand(ChannelHandlerContext ctx) {
      C command = this.getCommand(ctx.channel());
      boolean hasAppSeqCallback = false;

      if (command instanceof DscCommandWithAppSeq) {
         DscCommandWithAppSeq withSeq = (DscCommandWithAppSeq) command;
         if (withSeq.hasResponseCallback()) {
               throw new IllegalStateException("Response callback already set");
         }
         withSeq.setResponseCallback(new ResponseReceivedCallback());
         hasAppSeqCallback = true;
      }

      ctx.write(command)
         .addListener(new CommandSentCallback())
         .addListener(LogOnFailure.INSTANCE);

      if (!hasAppSeqCallback) {
         onSendSuccess(ctx);
      }
   }

   private class CommandSentCallback implements ChannelFutureListener {
      @Override
      public void operationComplete(ChannelFuture future) {
         if (future.isSuccess()) {
               onCommandSent(future.channel());
         } else {
               System.out.println("WARN: sending failed for " + commandClass.getSimpleName() + ": " + future.cause());
         }
      }
   }

   private class ResponseReceivedCallback implements DscCommandWithAppSeq.ResponseCallback {
      @Override
      public void generalResponseReceived(Channel channel, DscGeneralResponse response) {
         ChannelHandlerContext ctx = channel.pipeline().context(HandshakeHandler.this);
         if (response.isSuccess()) {
               onSendSuccess(ctx);
         } else {
               System.out.println("WARN: negative response for " + commandClass.getSimpleName() + ": " + response);
               onFailure(ctx);
         }
      }
   }
}
