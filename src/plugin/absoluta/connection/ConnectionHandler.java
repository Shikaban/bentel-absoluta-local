package plugin.absoluta.connection;

import cms.device.api.Panel;
import cms.device.spi.AlertCallback;
import protocol.dsc.DscEndpointState;
import protocol.dsc.DscError;
import protocol.dsc.Endpoint;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.Messenger;
import protocol.dsc.NewValue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;

public class ConnectionHandler {
   private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[0-9]{1,6}$");
   private static final int USER_ACCESS_LEVEL = 2;
   private static final int INVALID_ACCESS_CODE = 17;
   private final PanelStatus panelStatus;
   private final AlertCallback alertCallback;
   private String pin;
   private Endpoint endpoint;
   private Messenger messenger;
   private MessageHandler messageHandler;
   private StatusReader statusReader;
   private Commander commander;
   private Panel.ConnStatus connectionStatus;
   private boolean loggedIn;
   private boolean closing;
   private static final boolean VERBOSE_DEBUG = false;

   public ConnectionHandler(PanelStatus var1, AlertCallback var3) {
      this.panelStatus = (PanelStatus)Objects.requireNonNull(var1);
      this.alertCallback = (AlertCallback)Objects.requireNonNull(var3);
   }

   public boolean setPin(String pin) {
      if (pin != null && PASSWORD_PATTERN.matcher(pin).matches()) {
         this.pin = StringUtils.leftPad(pin, 6, 'A');
         return true;
      } else {
         return false;
      }
   }

   public synchronized Panel.ConnStatus waitConnection() throws InterruptedException {
      while(this.connectionStatus == null) {
         this.wait();
      }

      return this.connectionStatus;
   }

   public Commander getCommander() {
      return this.commander;
   }

   public void disconnect() {
      this.panelStatus.setConnectionStatus(PanelStatus.ConnectionStatus.DISCONNECTING);
      this.endpoint.close();
   }

   void setEndpoint(Endpoint var1) {
      if (this.endpoint != null) {
         throw new IllegalStateException("connected called twice");
      } else if (this.pin == null) {
         throw new IllegalStateException("no password set");
      } else {
         this.endpoint = (Endpoint)Objects.requireNonNull(var1);
         this.messenger = (Messenger)Objects.requireNonNull(var1.getMessenger());
         this.messageHandler = new MessageHandler(var1, new ConnectionHandler.ErrorListener());
         this.commander = new Commander(this.messageHandler, this.panelStatus);
         this.endpoint.addPropertyChangeListener(new ConnectionHandler.HandlerEndpointListener());
         this.statusReader = new StatusReader(this.messageHandler, var1.getExecutor());
         this.messenger.addMessageListener(this.statusReader);
         this.messenger.addMessageListener(new StatusListener(this.panelStatus));
         this.messenger.addMessageListener(new ConnectionHandler.HandlerMessageListener());
         this.messenger.addMessageListener(new AlertListener(this.alertCallback, this.panelStatus));
      }
   }

   void disconnected() {
      this.stop();
      this.panelStatus.setConnectionStatus(PanelStatus.ConnectionStatus.DISCONNECTED);
      this.setConnectionStatus(Panel.ConnStatus.UNREACHABLE);
   }

   private void stop() {
      if (!this.closing) {
         this.closing = true;
         if (this.endpoint != null) {
            assert this.messageHandler != null;
            this.messageHandler.stop();
            this.endpoint.close();
         }
      }
   }

   private synchronized void setConnectionStatus(Panel.ConnStatus var1) {
      if (this.connectionStatus == null) {
         this.connectionStatus = var1;
         this.notifyAll();
      }
   }

   class ErrorListener {
      private ErrorListener() {
      }

      void fatalError() {
         System.out.println("WARN: fatal error: closing connection");
         ConnectionHandler.this.endpoint.close();
      }

      // $FF: synthetic method
      ErrorListener(Object var2) {
         this();
      }
   }

   private class HandlerEndpointListener implements PropertyChangeListener {
      private HandlerEndpointListener() {
      }

      public void propertyChange(PropertyChangeEvent var1) {
         if ("state".equals(var1.getPropertyName())) {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: endpoint status: " + var1.getOldValue() + " -> " + var1.getNewValue());
            }
            switch((DscEndpointState)var1.getNewValue()) {
            case READY:
               ConnectionHandler.this.endpoint.setPin("");
               ConnectionHandler.this.messenger.send(Message.ENTER_ACCESS_LEVEL, Pair.with(USER_ACCESS_LEVEL, ConnectionHandler.this.pin));
               break;
            case CLOSING:
            case CLOSED:
               ConnectionHandler.this.stop();
            default:
               break;
            }
         }
      }
   }

   private class HandlerMessageListener implements MessageListener {
      private HandlerMessageListener() {
      }

      public void newValue(NewValue var1) {
         if (var1.isFor(Message.ENTER_ACCESS_LEVEL) && !ConnectionHandler.this.loggedIn) {
            ConnectionHandler.this.loggedIn = true;
            ConnectionHandler.this.panelStatus.setConnectionStatus(PanelStatus.ConnectionStatus.CONNECTED);
            ConnectionHandler.this.setConnectionStatus(Panel.ConnStatus.SUCCESS);
            ConnectionHandler.this.statusReader.startWaitingForNotificationsAfterLogin();
            ConnectionHandler.this.endpoint.setSessionful(true);
         }
      }

      public void error(DscError var1) {
         if (var1.isFor(Message.ENTER_ACCESS_LEVEL)) {
            if (var1.getResponseCode() == INVALID_ACCESS_CODE) {
               ConnectionHandler.this.setConnectionStatus(Panel.ConnStatus.UNAUTHORIZED);
            } else {
               ConnectionHandler.this.setConnectionStatus(Panel.ConnStatus.INCOMPATIBLE);
            }
            ConnectionHandler.this.stop();
         }
      }
   }
}