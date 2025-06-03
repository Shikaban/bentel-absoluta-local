package protocol.dsc.session;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import protocol.dsc.DscEndpointState;
import protocol.dsc.DscError;
import protocol.dsc.Endpoint;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.Messenger;
import protocol.dsc.NewValue;
import protocol.dsc.Priority;
import protocol.dsc.commands.EndSession;
import protocol.dsc.transport.EndpointHandler;
import protocol.dsc.transport.command_handlers.PollHandler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;

public class DscEndpoint implements Endpoint, Messenger {
   private final Channel channel;
   private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
   private final List<MessageListener> messageListeners = new CopyOnWriteArrayList<>();
   private String panelId;
   private String pin;
   private boolean sessionful;
   private DscEndpointState state;
   private static final boolean VERBOSE_DEBUG = false;

   public DscEndpoint(Channel channel) {
      this.channel = Preconditions.checkNotNull(channel);
   }

   public String getPanelId() {
      return this.panelId;
   }

   public void setPanelId(String newPanelId) {
      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: setting panel id: " + newPanelId);
      }
      String oldPanelId = this.panelId;
      this.panelId = newPanelId;
      this.changeSupport.firePropertyChange("panelId", oldPanelId, newPanelId);
   }

   public String getPin() {
      return this.pin;
   }

   public void setPin(String newPin) {
      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: setting pin: " + newPin);
      }
      String oldPin = this.pin;
      this.pin = newPin;
      EndpointHandler.setPin(this.channel, newPin);
      this.changeSupport.firePropertyChange("pin", oldPin, newPin);
   }

   public boolean isSessionful() {
      return this.sessionful;
   }

   public void setSessionful(boolean sessionful) {
      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: setting sessionful: " + sessionful);
      }
      boolean oldSessionful = this.sessionful;
      this.sessionful = sessionful;
      this.setPoller();
      this.changeSupport.firePropertyChange("sessionful", oldSessionful, sessionful);
   }

   public DscEndpointState getState() {
      return this.state;
   }

   public void setState(DscEndpointState newState) {
      DscEndpointState oldState = this.state;
      if (oldState == DscEndpointState.CLOSED) {
         System.out.println("WARN: Current status is CLOSED: ignoring the request to change to " + newState);
      } else {
         System.out.println("INFO: setting state: " + newState);
         this.state = newState;
         this.setPoller();
         this.changeSupport.firePropertyChange("state", oldState, newState);
      }
   }

   public void addPropertyChangeListener(PropertyChangeListener listener) {
      this.changeSupport.addPropertyChangeListener(listener);
   }

   public void removePropertyChangeListener(PropertyChangeListener listener) {
      this.changeSupport.removePropertyChangeListener(listener);
   }

   public void close() {
      if (this.state != DscEndpointState.CLOSING && this.state != DscEndpointState.CLOSED) {
         if (this.channel.isActive()) {
               if (VERBOSE_DEBUG) {
                  System.out.println("DEBUG: closing endpoint: sending end session");
               }
               EndSession endSession = new EndSession();
               endSession.setPriority(Priority.HIGH);
               this.channel.write(endSession);
         } else {
               if (VERBOSE_DEBUG) {
                  System.out.println("DEBUG: closing endpoint: closing channel");
               }
               this.channel.close();
         }
      }
   }

   public Messenger getMessenger() {
      return this;
   }

   public ScheduledExecutorService getExecutor() {
      return this.channel.eventLoop();
   }

   public <V> void send(Message<Void, V> message) {
      this.send(message, Priority.NORMAL);
   }

   public <V> void send(Message<Void, V> message, Priority priority) {
      this.send(message, null, priority);
   }

   public <P, V> void send(Message<P, V> message, P param) {
      this.send(message, param, Priority.NORMAL);
   }

   public <P, V> void send(final Message<P, V> message, final P param, Priority priority) {
      Preconditions.checkNotNull(message);
      Preconditions.checkNotNull(priority);
      SendingMessage<P, V> sendingMessage = new SendingMessage<>(message, param, priority);
      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: sending: " + sendingMessage);
      }
      this.channel.write(sendingMessage).addListener(new ChannelFutureListener() {
         @Override
         public void operationComplete(ChannelFuture future) throws Exception {
               Throwable cause = future.cause();
               if (cause != null) {
                  DscEndpoint.this.broadcastError(DscError.newMessageError(message, param, cause));
               }
         }
      });
   }

   public void addMessageListener(MessageListener listener) {
      this.messageListeners.add(listener);
   }

   public void removeMessageListener(MessageListener listener) {
      this.messageListeners.remove(listener);
   }

   public void broadcastNewValue(NewValue value) {
      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: new value received: " + value);
      }
      for (MessageListener listener : this.messageListeners) {
         listener.newValue(value);
      }
   }

   public void broadcastError(DscError error) {
      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: error received: " + error);
      }
      for (MessageListener listener : this.messageListeners) {
         listener.error(error);
      }
   }

   private void setPoller() {
      PollHandler.setPollEnabled(this.channel, this.sessionful && this.state == DscEndpointState.READY);
   }
}
