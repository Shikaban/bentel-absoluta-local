package cms.device.api;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

public final class AlertNotifier {
   private static final AlertNotifier INSTANCE = new AlertNotifier();
   private final List<AlertListener> listeners = new CopyOnWriteArrayList<AlertListener>();

   public static AlertNotifier getDefault() {
      return INSTANCE;
   }

   void fire(Object var1, String var2) {
      final AlertEvent var3 = new AlertEvent((String)Preconditions.checkNotNull(var2), Preconditions.checkNotNull(var1));
      System.out.println("TRACE: firing new event: " + var3);
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            for (AlertListener listener : AlertNotifier.this.listeners) {
               try {
                  listener.alertEventReceived(var3);
               } catch (RuntimeException ex) {
                  System.out.println("WARN: exception in event listener: " + ex);
               }
            }
         }
      });
   }
}