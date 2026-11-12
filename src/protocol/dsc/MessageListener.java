package protocol.dsc;

import java.util.EventListener;

public interface MessageListener extends EventListener {
   void newValue(NewValue newValue2);

   void error(DscError error2);
}
