package protocol.dsc;

public interface DscServer {
   void start();

   void stop() throws InterruptedException;

   void addIncomingConnectionListener(IncomingConnectionListener var1);

   void removeIncomingConnectionListener(IncomingConnectionListener var1);

}
