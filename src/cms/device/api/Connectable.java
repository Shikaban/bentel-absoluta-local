package cms.device.api;

public interface Connectable {
   Panel.connStatus connect();

   void disconnect();

   boolean isConnected();

}
