package cms.device.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

public class Partition implements NotifierEventSource, ChangeEventSource {
   private String id;
   private String remoteName;
   private Partition.Arming arming;
   private Partition.Status status;
   private final List<String> inputsIds;
   private final Panel panel;
   private final ChangeSupport changeSupport;
   private boolean iconIsAlarmed;

   public Partition(Panel var1) {
      this.arming = Partition.Arming.DISARMED;
      this.status = Partition.Status.OK;
      this.panel = var1;
      this.iconIsAlarmed = false;
      this.inputsIds = new ArrayList();
      this.changeSupport = new ChangeSupport(this);
   }

   public DeviceOrPanel getParent() {
      return this.panel;
   }

   public int getNumber() {
      return -1;
   }

   public Panel getPanel() {
      return this.panel;
   }

   public String getId() {
      if (this.id == null) {
         Iterator var1 = this.getPanel().getPartitions().entrySet().iterator();

         while(var1.hasNext()) {
            Entry<String, Partition> var2 = (Entry)var1.next();
            if (((Partition)var2.getValue()).equals(this)) {
               return (String)var2.getKey();
            }
         }
      }

      return this.id;
   }

   public void setId(String var1) {
      assert var1 != null && !var1.isEmpty();

      this.id = var1;
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

   public void addChangeListener(ChangeListener var1) {
      this.changeSupport.addChangeListener(var1);
   }

   public void removeChangeListener(ChangeListener var1) {
      this.changeSupport.removeChangeListener(var1);
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

   public boolean iconIsAlarmed() {
      return this.iconIsAlarmed;
   }

   public void setIconState(boolean var1) {
      this.iconIsAlarmed = var1;
      this.fireChange();
   }

   public static enum Arming {
      DISARMED,
      AWAY,
      STAY,
      NODELAY;
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
