package protocol.dsc;

import java.beans.PropertyChangeListener;
import java.util.concurrent.ScheduledExecutorService;

public interface Endpoint {
   String PROP_PANEL_ID = "panelId";
   String PROP_PIN = "pin";
   String PROP_SESSIONFUL = "sessionful";
   String PROP_STATE = "state";

   String getPanelId();

   String getPin();

   void setPin(String id);

   boolean isSessionful();

   void setSessionful(boolean enabled);

   DscEndpointState getState();

   void addPropertyChangeListener(PropertyChangeListener propertyChangeListener2);

   void removePropertyChangeListener(PropertyChangeListener propertyChangeListener2);

   void close();

   Messenger getMessenger();

   ScheduledExecutorService getExecutor();
}
