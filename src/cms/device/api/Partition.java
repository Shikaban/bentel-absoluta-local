package cms.device.api;

import java.util.ArrayList;
import java.util.Collections;
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

   public synchronized void addInputs(List<String> newInputIds) {
      // Rimuovi gli input che non sono più presenti
      inputsIds.removeIf(existingId -> !newInputIds.contains(existingId));

      // Aggiungi i nuovi input che non sono già presenti
      for (String inputId : newInputIds) {
         if (!inputsIds.contains(inputId)) {
            inputsIds.add(inputId);
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