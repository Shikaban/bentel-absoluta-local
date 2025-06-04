package cms.device.api;

public interface Connectable {
   Panel.ConnStatus connect();

   void disconnect();

   boolean isConnected();

}
