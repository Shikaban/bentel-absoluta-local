package protocol.dsc.transport;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import protocol.dsc.DscEndpointState;
import protocol.dsc.DscError;
import protocol.dsc.NewValue;
import protocol.dsc.session.DscEndpoint;
import protocol.dsc.session.SendingMessage;
import protocol.dsc.session.SessionInfo;
import protocol.dsc.util.LogOnFailure;

import java.util.concurrent.TimeUnit;

public class EndpointHandler extends ChannelDuplexHandler {
   private static final AttributeKey<String> PIN_KEY = AttributeKey.valueOf("EndpointHandler.pin");
   private final DscEndpoint endpoint;
   private String panelId;

   public EndpointHandler(DscEndpoint var1) {
      this.endpoint = (DscEndpoint)Preconditions.checkNotNull(var1);
   }

   public void write(ChannelHandlerContext var1, Object var2, ChannelPromise var3) throws Exception {
      if (var2 instanceof SendingMessage && this.endpoint.getState() != DscEndpointState.READY) {
         var3.setFailure(new IllegalStateException("invalid state at the sending time"));
      } else {
         var3.addListener(LogOnFailure.INSTANCE);
         var1.write(var2, var3);
      }

   }

   public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
      if (this.panelId == null) {
         this.panelId = SessionInfo.getPeerInfo(var1.channel()).getMultiPointCommId();
         if (this.panelId != null) {
            this.endpoint.setPanelId(this.panelId);
         }
      }

      if (var2 instanceof NewValue) {
         this.endpoint.broadcastNewValue((NewValue)var2);
      } else if (var2 instanceof DscError) {
         this.endpoint.broadcastError((DscError)var2);
      } else {
         super.channelRead(var1, var2);
      }

   }

   public void userEventTriggered(ChannelHandlerContext var1, Object var2) throws Exception {
      if (var2 == SimpleMessage.CLOSING_CHANNEL_EVENT) {
         this.closingChannel(var1);
      } else if (var2 == SimpleMessage.HANDSHAKE_BEGIN_EVENT) {
         this.endpoint.setState(DscEndpointState.HANDSHAKING);
      } else if (var2 == SimpleMessage.HANDSHAKE_END_EVENT) {
         this.endpoint.setState(DscEndpointState.READY);
      } else {
         super.userEventTriggered(var1, var2);
      }

   }

   private void closingChannel(ChannelHandlerContext var1) {
      this.endpoint.setState(DscEndpointState.CLOSING);
      final Channel var2 = var1.channel();
      var1.executor().schedule(new Runnable() {
         public void run() {
            if (var2.isOpen()) {
               System.out.println("WARN: forcing channel closure ...");
               var2.close().addListener(LogOnFailure.INSTANCE);
            }

         }
      }, 20L, TimeUnit.SECONDS);
   }

   public static String getPin(Channel var0) {
      return (String)var0.attr(PIN_KEY).get();
   }

   public static void setPin(Channel var0, String var1) {
      var0.attr(PIN_KEY).set(var1);
   }
}
