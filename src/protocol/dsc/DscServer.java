package protocol.dsc;

public interface DscServer {
   void start();

   void stop() throws InterruptedException;

   void addIncomingConnectionListener(IncomingConnectionListener incomingConnectionListener2);

   void removeIncomingConnectionListener(IncomingConnectionListener incomingConnectionListener2);

}
