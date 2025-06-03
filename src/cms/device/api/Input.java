package cms.device.api;

import java.util.Objects;

import org.openide.util.ChangeSupport;

public class Input {
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

      void setRemoteName(String name) {
      String clean = name != null && !name.trim().isEmpty() ? name.trim() : null;
      if (!Objects.equals(this.remoteName, clean)) {
         this.remoteName = clean;
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
