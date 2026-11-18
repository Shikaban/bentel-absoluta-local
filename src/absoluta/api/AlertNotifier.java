package absoluta.api;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public final class AlertNotifier {
   private static final Logger logger = Logger.getLogger(AlertNotifier.class.getName());
   // Singleton for centralized alarm notification management
   private static final AlertNotifier INSTANCE = new AlertNotifier();

   // Thread-safe list of registered listeners
   private final List<AlertListener> listeners = new CopyOnWriteArrayList<>();

   public static AlertNotifier getDefault() {
      return INSTANCE;
   }

   // Notify all listeners of a new alarm event.
   void fire(Object source, String message) {
      final AlertEvent alertEvent = new AlertEvent(
         Preconditions.checkNotNull(message),
         Preconditions.checkNotNull(source)
      );
      logger.finer("Firing new event: " + alertEvent);

      // Notify the listeners
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
               for (AlertListener listener : AlertNotifier.this.listeners) {
                  try {
                     listener.alertEventReceived(alertEvent);
                  } catch (RuntimeException ex) {
                     logger.severe("Exception in event listener: " + ex);
                  }
               }
         }
      });
   }
}