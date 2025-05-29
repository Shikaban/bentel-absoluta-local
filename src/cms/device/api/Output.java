package cms.device.api;

import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

public class Output implements NotifierEventSource, ChangeEventSource {
   private final String id;
   private String remoteName;
   private boolean enabled = true;
   private Output.Type type;
   private Output.Status status;
   private final DeviceOrPanel deviceOrPanel;
   private final Consumer<Output.Action> controller;
   private final ChangeSupport changeSupport;

   Output(DeviceOrPanel var1, String var2, Consumer<Output.Action> var3) {
      this.type = Output.Type.BISTABLE;
      this.status = Output.Status.UNKNOWN;
      this.deviceOrPanel = (DeviceOrPanel)Objects.requireNonNull(var1);
      this.id = (String)Objects.requireNonNull(var2);
      this.controller = (Consumer)Objects.requireNonNull(var3);
      this.changeSupport = new ChangeSupport(this);
   }

   public DeviceOrPanel getParent() {
      return this.deviceOrPanel;
   }

   public int getNumber() {
      DeviceOrPanel var1 = this.getParent();
      if (var1 instanceof Device) {
         Device var2 = (Device)var1;
         return var2.getOutputs().indexOf(this);
      } else {
         return -1;
      }
   }

   public String getId() {
      return this.id;
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

   public Output.Type getType() {
      return this.type;
   }

   public void setType(Output.Type var1) {
      if (this.type != Objects.requireNonNull(var1)) {
         this.type = var1;
         this.changeSupport.fireChange();
      }

   }

   public Output.Status getStatus() {
      return this.status;
   }

   public void setStatus(Output.Status var1) {
      if (this.status != Objects.requireNonNull(var1)) {
         this.status = var1;
         this.changeSupport.fireChange();
      }

   }

   public boolean canDo(Output.Action var1) {
      if (!this.enabled) {
         return false;
      } else {
         switch(this.type) {
         case MONOSTABLE:
            return var1 == Output.Action.DO_IMPULSE;
         case BISTABLE:
            switch(this.status) {
            case CLOSED:
               return var1 == Output.Action.DO_OPEN;
            case OPEN:
               return var1 == Output.Action.DO_CLOSE;
            default:
               return var1 == Output.Action.DO_OPEN || var1 == Output.Action.DO_CLOSE;
            }
         default:
            return false;
         }
      }
   }

   public void doAction(Output.Action var1) {
      this.controller.accept(var1);
   }

   public void addChangeListener(ChangeListener var1) {
      this.changeSupport.addChangeListener(var1);
   }

   public void removeChangeListener(ChangeListener var1) {
      this.changeSupport.removeChangeListener(var1);
   }

   void fireChange() {
      this.changeSupport.fireChange();
   }

   public static enum Action {
      DO_IMPULSE,
      DO_OPEN,
      DO_CLOSE;
   }

   public static enum Status {
      UNKNOWN,
      OPEN,
      CLOSED;
   }

   public static enum Type {
      BISTABLE,
      MONOSTABLE;
   }
}
