package cms.device.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.util.ChangeSupport;

public class Partition {
   private String remoteName;
   private Partition.Arming arming;
   private Partition.Status status;
   private final List<String> inputsIds;
   private final ChangeSupport changeSupport;

   public Partition(Panel var1) {
      this.arming = Partition.Arming.NOT_AVAILABLE;
      this.status = Partition.Status.OK;
      this.inputsIds = new ArrayList();
      this.changeSupport = new ChangeSupport(this);
   }

   public String getRemoteName() {
      return this.remoteName;
   }

   void setRemoteName(String var1) {
      this.remoteName = Device.sanitize(var1);
      this.fireChange();
   }

   public Partition.Arming getArming() {
      return this.arming;
   }

   void setArming(Partition.Arming var1) {
      this.arming = var1;
      this.fireChange();
   }

   public Partition.Status getStatus() {
      return this.status;
   }

   void setStatus(Partition.Status var1) {
      this.status = var1;
      this.fireChange();
   }

   public void fireChange() {
      this.changeSupport.fireChange();
   }

   public synchronized void addInputs(List<String> var1) {
      Iterator var2 = this.inputsIds.iterator();

      String var3;
      while(var2.hasNext()) {
         var3 = (String)var2.next();
         if (!var1.contains(var3)) {
            this.inputsIds.remove(var3);
         }
      }

      var2 = var1.iterator();

      while(var2.hasNext()) {
         var3 = (String)var2.next();
         if (!this.inputsIds.contains(var3)) {
            this.inputsIds.add(var3);
         }
      }

   }

   public List<String> getInputs() {
      return Collections.unmodifiableList(this.inputsIds);
   }

   public static enum Arming {
      DISARMED,
      AWAY,
      STAY,
      NODELAY,
      TRIGGERED,
      NOT_AVAILABLE;
   }

   public static enum Status {
      FIRE,
      TAMPER,
      FAULTS,
      ALARMS,
      ACTIVE,
      OK;
   }
}