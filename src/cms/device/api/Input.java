package cms.device.api;

import java.util.Objects;

import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

public class Input implements ChangeEventSource {
   private String remoteName;
   private Input.Status status;
   private final ChangeSupport changeSupport;

   public Input(Panel var1) {
      this.status = Input.Status.OK;
      this.changeSupport = new ChangeSupport(this);
   }

   public String getRemoteName() {
      return this.remoteName;
   }

      void setRemoteName(String var1) {
      String var2 = Device.sanitize(var1);
      if (!Objects.equals(this.remoteName, var2)) {
         this.remoteName = var2;
         this.changeSupport.fireChange();
      }
   }

   public Input.Status getStatus() {
      return this.status;
   }

   public void setStatus(Input.Status var1) {
      if (this.status != var1) {
         this.status = var1;
         this.changeSupport.fireChange();
      }
   }

   public void addChangeListener(ChangeListener var1) {
      this.changeSupport.addChangeListener(var1);
   }

   public void removeChangeListener(ChangeListener var1) {
      this.changeSupport.removeChangeListener(var1);
   }

   public void fireChange() {
      this.changeSupport.fireChange();
   }

   public static enum Status {
      TAMPER,
      FAULT,
      ALARM,
      ACTIVE,
      BYPASSED,
      OK;
   }
}
