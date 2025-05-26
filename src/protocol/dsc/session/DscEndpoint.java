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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;

public class DscEndpoint implements Endpoint, Messenger {
   private final Channel channel;
   private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
   private final List<MessageListener> messageListeners = new CopyOnWriteArrayList();
   private String panelId;
   private String pin;
   private boolean sessionful;
   private DscEndpointState state;
   private static final boolean VERBOSE_DEBUG = false;

   public DscEndpoint(Channel var1) {
      this.channel = (Channel)Preconditions.checkNotNull(var1);
   }

   public String getPanelId() {
      return this.panelId;
   }

   public void setPanelId(String newpanelID) {
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: setting panel id: " + newpanelID);
      }
      String oldPanelID = this.panelId;
      this.panelId = newpanelID;
      this.changeSupport.firePropertyChange("panelId", oldPanelID, newpanelID);
   }

   public String getPin() {
      return this.pin;
   }

   public void setPin(String newPin) {
      if(VERBOSE_DEBUG) {
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

   public void setSessionful(boolean var1) {
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: setting sessionful: " + var1);
      }
      boolean var2 = this.sessionful;
      this.sessionful = var1;
      this.setPoller();
      this.changeSupport.firePropertyChange("sessionful", var2, var1);
   }

   public DscEndpointState getState() {
      return this.state;
   }

   public void setState(DscEndpointState var1) {
      DscEndpointState var2 = this.state;
      if (var2 == DscEndpointState.CLOSED) {
         System.out.println("WARN: Current status is CLOSED: ignoring the request to change to " + var1);
      } else {
         System.out.println("INFO: setting state: " + var1);
         this.state = var1;
         this.setPoller();
         this.changeSupport.firePropertyChange("state", var2, var1);
      }
   }

   public void addPropertyChangeListener(PropertyChangeListener var1) {
      this.changeSupport.addPropertyChangeListener(var1);
   }

   public void removePropertyChangeListener(PropertyChangeListener var1) {
      this.changeSupport.removePropertyChangeListener(var1);
   }

   public void close() {
      if (this.state != DscEndpointState.CLOSING && this.state != DscEndpointState.CLOSED) {
         if (this.channel.isActive()) {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: closing endpoint: sending end session");
            }
            EndSession var1 = new EndSession();
            var1.setPriority(Priority.HIGH);
            this.channel.write(var1);
         } else {
            if(VERBOSE_DEBUG) {
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

   public <V> void send(Message<Void, V> var1) {
      this.send(var1, Priority.NORMAL);
   }

   public <V> void send(Message<Void, V> var1, Priority var2) {
      this.send(var1, null, var2);
   }

   public <P, V> void send(Message<P, V> var1, P var2) {
      this.send(var1, var2, Priority.NORMAL);
   }

   public <P, V> void send(final Message<P, V> var1, final P var2, Priority var3) {
      Preconditions.checkNotNull(var1);
      Preconditions.checkNotNull(var3);
      SendingMessage<P, V> var4 = new SendingMessage(var1, var2, var3);
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: sending: " + var4);
      }
      this.channel.write(var4).addListener(new ChannelFutureListener() {
         public void operationComplete(ChannelFuture var1x) throws Exception {
            Throwable var2x = var1x.cause();
            if (var2x != null) {
               DscEndpoint.this.broadcastError(DscError.newMessageError(var1, var2, var2x));
            }

         }
      });
   }

   public void addMessageListener(MessageListener var1) {
      this.messageListeners.add(var1);
   }

   public void removeMessageListener(MessageListener var1) {
      this.messageListeners.remove(var1);
   }

   public void broadcastNewValue(NewValue var1) {
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: new value received: " + var1);
      }
      Iterator var2 = this.messageListeners.iterator();

      while(var2.hasNext()) {
         MessageListener var3 = (MessageListener)var2.next();
         var3.newValue(var1);
      }
   }

   public void broadcastError(DscError var1) {
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: error received: " + var1);
      }
      Iterator var2 = this.messageListeners.iterator();

      while(var2.hasNext()) {
         MessageListener var3 = (MessageListener)var2.next();
         var3.error(var1);
      }

   }

   private void setPoller() {
      PollHandler.setPollEnabled(this.channel, this.sessionful && this.state == DscEndpointState.READY);
   }
}
