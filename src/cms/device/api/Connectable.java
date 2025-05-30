package cms.device.api;

public interface Connectable extends ChangeEventSource {
   Panel.connStatus connect();

   void disconnect();

   boolean isConnected();

}
