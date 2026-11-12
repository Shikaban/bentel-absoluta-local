package absoluta.connection;

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
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;

import absoluta.AbsolutaPanelProvider.providerConnStatus;
import absoluta.connection.PanelStatus.PanelConnStatus;
import absoluta.spi.AlertCallback;

public class ConnectionHandler {
   private static final Logger logger = Logger.getLogger(ConnectionHandler.class.getName());
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
   private providerConnStatus connectionStatus;
   private boolean loggedIn;
   private boolean closing;

   public ConnectionHandler(PanelStatus panelStatus2, AlertCallback alertCallback2) {
      this.panelStatus = (PanelStatus)Objects.requireNonNull(panelStatus2);
      this.alertCallback = (AlertCallback)Objects.requireNonNull(alertCallback2);
   }

   public boolean setPin(String pin) {
      if (pin != null && PASSWORD_PATTERN.matcher(pin).matches()) {
         this.pin = StringUtils.leftPad(pin, 6, 'A');
         return true;
      } else {
         return false;
      }
   }

   public synchronized providerConnStatus waitConnection() throws InterruptedException {
      while(this.connectionStatus == null) {
         this.wait();
      }

      return this.connectionStatus;
   }

   public Commander getCommander() {
      return this.commander;
   }

   public void disconnect() {
      this.panelStatus.updateConnectionStatus(PanelConnStatus.DISCONNECTING);
      this.endpoint.close();
   }

   void setEndpoint(Endpoint endpoint2) {
      if (this.endpoint != null) {
         throw new IllegalStateException("connected called twice");
      } else if (this.pin == null) {
         throw new IllegalStateException("no password set");
      } else {
         this.endpoint = (Endpoint)Objects.requireNonNull(endpoint2);
         this.messenger = (Messenger)Objects.requireNonNull(endpoint2.getMessenger());
         this.messageHandler = new MessageHandler(endpoint2, new ConnectionHandler.ErrorListener());
         this.commander = new Commander(this.messageHandler, this.panelStatus);
         this.endpoint.addPropertyChangeListener(new ConnectionHandler.HandlerEndpointListener());
         this.statusReader = new StatusReader(this.messageHandler, endpoint2.getExecutor());
         this.messenger.addMessageListener(this.statusReader);
         this.messenger.addMessageListener(new StatusListener(this.panelStatus));
         this.messenger.addMessageListener(new ConnectionHandler.HandlerMessageListener());
         this.messenger.addMessageListener(new AlertListener(this.alertCallback, this.panelStatus));
      }
   }

   void disconnected() {
      this.stop();
      this.panelStatus.updateConnectionStatus(PanelConnStatus.DISCONNECTED);
      this.setConnectionStatus(providerConnStatus.UNREACHABLE);
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

   private synchronized void setConnectionStatus(providerConnStatus providerConnStatus2) {
      if (this.connectionStatus == null) {
         this.connectionStatus = providerConnStatus2;
         this.notifyAll();
      }
   }

   class ErrorListener {
      private ErrorListener() {
      }

      void fatalError() {
         logger.severe("Fatal error: closing connection");
         ConnectionHandler.this.endpoint.close();
      }

      // $FF: synthetic method
      ErrorListener(Object object2) {
         this();
      }
   }

   private class HandlerEndpointListener implements PropertyChangeListener {
      private HandlerEndpointListener() {
      }

      public void propertyChange(PropertyChangeEvent propertyChangeEvent2) {
         if ("state".equals(propertyChangeEvent2.getPropertyName())) {
            logger.fine("Endpoint status: " + propertyChangeEvent2.getOldValue() + " -> " + propertyChangeEvent2.getNewValue());
            switch((DscEndpointState)propertyChangeEvent2.getNewValue()) {
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

      public void newValue(NewValue newValue2) {
         if (newValue2.isFor(Message.ENTER_ACCESS_LEVEL) && !ConnectionHandler.this.loggedIn) {
            ConnectionHandler.this.loggedIn = true;
            ConnectionHandler.this.panelStatus.updateConnectionStatus(PanelConnStatus.CONNECTED);
            ConnectionHandler.this.setConnectionStatus(providerConnStatus.SUCCESS);
            ConnectionHandler.this.statusReader.startWaitingForNotificationsAfterLogin();
            ConnectionHandler.this.endpoint.setSessionful(true);
         }
      }

      public void error(DscError error2) {
         if (error2.isFor(Message.ENTER_ACCESS_LEVEL)) {
            if (error2.getResponseCode() == INVALID_ACCESS_CODE) {
               ConnectionHandler.this.setConnectionStatus(providerConnStatus.UNAUTHORIZED);
            } else {
               ConnectionHandler.this.setConnectionStatus(providerConnStatus.INCOMPATIBLE);
            }
            ConnectionHandler.this.stop();
         }
      }
   }
}