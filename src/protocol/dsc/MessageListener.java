package protocol.dsc;

import java.util.EventListener;

public interface MessageListener extends EventListener {
   void newValue(NewValue var1);

   void error(DscError var1);
}
