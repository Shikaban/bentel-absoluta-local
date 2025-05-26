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

   void setPin(String var1);

   boolean isSessionful();

   void setSessionful(boolean var1);

   DscEndpointState getState();

   void addPropertyChangeListener(PropertyChangeListener var1);

   void removePropertyChangeListener(PropertyChangeListener var1);

   void close();

   Messenger getMessenger();

   ScheduledExecutorService getExecutor();
}
