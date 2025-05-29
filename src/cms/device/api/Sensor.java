package cms.device.api;

import java.util.Objects;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;


public final class Sensor implements NotifierEventSource, ChangeEventSource {
   private final ChangeSupport changeSupport;
   private final Device device;
   private String remoteName;
   private boolean enabled;
   private String id;

   Sensor(Device var1) {
      this.device = var1;
      this.changeSupport = new ChangeSupport(this);
      this.enabled = true;
   }

   public Device getParent() {
      return this.device;
   }

   public int getNumber() {
      return this.getParent().getSensors().indexOf(this);
   }

   public String getId() {
      return this.id == null ? Integer.toString(this.getNumber()) : this.id;
   }

   void setId(String var1) {
      assert var1 == null || !var1.isEmpty();

      this.id = var1;
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

   public boolean isEnabled() {
      return this.enabled;
   }

   void setEnabled(boolean var1) {
      if (this.enabled != var1) {
         this.enabled = var1;
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
}
