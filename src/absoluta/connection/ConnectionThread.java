
package absoluta.connection;

import java.util.Objects;
import java.util.logging.Logger;

import protocol.dsc.AbsolutaClient;
import protocol.dsc.Endpoint;
import protocol.dsc.ITv2Client;
import protocol.dsc.ITv2Client.Callback;

public class ConnectionThread extends Thread {
   private static final Logger logger = Logger.getLogger(ConnectionThread.class.getName());
   private final ITv2Client client;
   private final ConnectionHandler connectionHandler;

   public ConnectionThread(String text, int numericValue, ConnectionHandler connectionHandler2) {
      super(String.format("Absoluta connection thread %s:%d", text, numericValue));
      this.client = new AbsolutaClient(text, numericValue);
      this.connectionHandler = (ConnectionHandler)Objects.requireNonNull(connectionHandler2);
   }

   public void run() {
      try {
         this.client.connect(new Callback() {
            public void connected(Endpoint endpoint2) {
               ConnectionThread.this.connectionHandler.setEndpoint(endpoint2);
            }
         });
      } catch (RuntimeException | InterruptedException ex) {
         logger.severe("Error during connection: " + ex);
      } finally {
         logger.info("Connection closed");
         this.connectionHandler.disconnected();
      }
   }
}
