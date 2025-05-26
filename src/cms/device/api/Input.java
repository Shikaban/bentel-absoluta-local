package cms.device.api;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

import cms.device.spi.Plugin;

public class Input implements NotifierEventSource, ChangeEventSource {
   private String id;
   private String localName;
   private String remoteName;
   private Input.Status status;
   private final ChangeSupport changeSupport;
   private final Panel panelParent;

   public Input(Panel var1) {
      this.status = Input.Status.OK;
      this.panelParent = var1;
      this.changeSupport = new ChangeSupport(this);
   }

   public Panel getParent() {
      return this.panelParent;
   }

   public int getNumber() {
      return -1;
   }

   public String getId() {
      if (this.id == null) {
         Iterator var1 = this.getParent().getInputs().entrySet().iterator();

         while(var1.hasNext()) {
            Entry<String, Input> var2 = (Entry)var1.next();
            if (((Input)var2.getValue()).equals(this)) {
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

   public String getLocalName() {
      return this.localName;
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

   public String getName() {
      if (this.getLocalName() != null) {
         return this.getLocalName();
      } else {
         return this.getRemoteName() != null ? this.getRemoteName() : NbBundle.getMessage(Plugin.class, "LBL_DefaultInputName", this.id);
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

   public String toString() {
      return this.getName();
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
