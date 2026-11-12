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
import java.util.logging.Logger;

public class EndpointHandler extends ChannelDuplexHandler {
   private static final Logger logger = Logger.getLogger(EndpointHandler.class.getName());
   private static final AttributeKey<String> PIN_KEY = AttributeKey.valueOf("EndpointHandler.pin");
   private final DscEndpoint endpoint;
   private String panelId;

   public EndpointHandler(DscEndpoint endpoint2) {
      this.endpoint = (DscEndpoint)Preconditions.checkNotNull(endpoint2);
   }

   public void write(ChannelHandlerContext context, Object object2, ChannelPromise promise) throws Exception {
      if (object2 instanceof SendingMessage && this.endpoint.getState() != DscEndpointState.READY) {
         promise.setFailure(new IllegalStateException("invalid state at the sending time"));
      } else {
         promise.addListener(LogOnFailure.INSTANCE);
         context.write(object2, promise);
      }

   }

   public void channelRead(ChannelHandlerContext context, Object object2) throws Exception {
      if (this.panelId == null) {
         this.panelId = SessionInfo.getPeerInfo(context.channel()).getMultiPointCommId();
         if (this.panelId != null) {
            this.endpoint.setPanelId(this.panelId);
         }
      }

      if (object2 instanceof NewValue) {
         this.endpoint.broadcastNewValue((NewValue)object2);
      } else if (object2 instanceof DscError) {
         this.endpoint.broadcastError((DscError)object2);
      } else {
         super.channelRead(context, object2);
      }

   }

   public void userEventTriggered(ChannelHandlerContext context, Object object2) throws Exception {
      if (object2 == SimpleMessage.CLOSING_CHANNEL_EVENT) {
         this.closingChannel(context);
      } else if (object2 == SimpleMessage.HANDSHAKE_BEGIN_EVENT) {
         this.endpoint.setState(DscEndpointState.HANDSHAKING);
      } else if (object2 == SimpleMessage.HANDSHAKE_END_EVENT) {
         this.endpoint.setState(DscEndpointState.READY);
      } else {
         super.userEventTriggered(context, object2);
      }

   }

   private void closingChannel(ChannelHandlerContext context) {
      this.endpoint.setState(DscEndpointState.CLOSING);
      final Channel channel2 = context.channel();
      context.executor().schedule(new Runnable() {
         public void run() {
            if (channel2.isOpen()) {
               logger.warning("Forcing channel closure ...");
               channel2.close().addListener(LogOnFailure.INSTANCE);
            }

         }
      }, 20L, TimeUnit.SECONDS);
   }

   public static String getPin(Channel channel2) {
      return (String)channel2.attr(PIN_KEY).get();
   }

   public static void setPin(Channel channel2, String id) {
      channel2.attr(PIN_KEY).set(id);
   }
}
