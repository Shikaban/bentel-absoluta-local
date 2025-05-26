package cms.device.api;

import java.util.EventObject;

public class AlertEvent extends EventObject {
   private final String message;

   public AlertEvent(String var1, Object var2) {
      super(var2);
      this.message = var1;
   }

   public String getMessage() {
      return this.message;
   }

   public String toString() {
      return "AlertEvent{message=" + this.message + ", source=" + this.source + '}';
   }
}
