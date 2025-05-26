
package plugin.absoluta.connection;

import java.util.Objects;

import protocol.dsc.AbsolutaClient;
import protocol.dsc.Endpoint;
import protocol.dsc.ITv2Client;
import protocol.dsc.ITv2Client.Callback;

public class ConnectionThread extends Thread {
   private final ITv2Client client;
   private final ConnectionHandler connectionHandler;

   public ConnectionThread(String var1, int var2, ConnectionHandler var3) {
      super(String.format("Absoluta connection thread %s:%d", var1, var2));
      this.client = new AbsolutaClient(var1, var2);
      this.connectionHandler = (ConnectionHandler)Objects.requireNonNull(var3);
   }

   public void run() {
      try {
         this.client.connect(new Callback() {
            public void connected(Endpoint var1) {
               ConnectionThread.this.connectionHandler.setEndpoint(var1);
            }
         });
      } catch (RuntimeException | InterruptedException ex) {
         System.out.println("DEBUG: error during connection: " + ex);
      } finally {
         System.out.println("Connection closed");
         this.connectionHandler.disconnected();
      }
   }
}
