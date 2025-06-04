package cms.device.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.util.ChangeSupport;

public class Partition {
   private String remoteName;
   private Arming arming;
   private Status status;
   private final List<String> inputIds;
   private final ChangeSupport changeSupport;

   public Partition(Panel panel) {
      this.arming = Arming.NOT_AVAILABLE;
      this.status = Status.OK;
      this.inputIds = new ArrayList<>();
      this.changeSupport = new ChangeSupport(this);
   }

   public String getRemoteName() {
      return remoteName;
   }

   void setRemoteName(String name) {
      this.remoteName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
      fireChange();
   }

   public Arming getArming() {
      return arming;
   }

   void setArming(Arming arming) {
      this.arming = arming;
      fireChange();
   }

   public Status getStatus() {
      return status;
   }

   void setStatus(Status status) {
      this.status = status;
      fireChange();
   }

   public void fireChange() {
      changeSupport.fireChange();
   }

   public synchronized void addInputs(List<String> newInputIds) {
      // Rimuovi gli input che non sono più presenti
      inputIds.removeIf(existingId -> !newInputIds.contains(existingId));

      // Aggiungi i nuovi input che non sono già presenti
      for (String inputId : newInputIds) {
         if (!inputIds.contains(inputId)) {
               inputIds.add(inputId);
         }
      }
   }

   public List<String> getInputs() {
      return Collections.unmodifiableList(inputIds);
   }

   public enum Arming {
      DISARMED,
      AWAY,
      STAY,
      NODELAY,
      TRIGGERED,
      NOT_AVAILABLE
   }

   public enum Status {
      FIRE,
      TAMPER,
      FAULTS,
      ALARMS,
      ACTIVE,
      OK
   }
}