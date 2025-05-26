package protocol.dsc;

import java.util.EventListener;

public interface IncomingConnectionListener extends EventListener {
   void deviceConnected(Endpoint var1);
}
