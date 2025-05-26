package cms.device.api;

import javax.swing.event.ChangeListener;

public interface ChangeEventSource {
   void addChangeListener(ChangeListener var1);

   void removeChangeListener(ChangeListener var1);
}
