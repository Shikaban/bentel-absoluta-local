package cms.device.api;

public interface Connectable extends ChangeEventSource {
   Device.Status connect();

   void disconnect();

   boolean isConnected();

   void dispose();
}
