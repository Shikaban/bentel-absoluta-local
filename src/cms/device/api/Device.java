package cms.device.api;

import com.google.common.collect.ImmutableList;
import cms.device.spi.OutputControlCookie;
import cms.device.spi.OutputControlProvider;

import cms.device.spi.DeviceProvider;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public final class Device implements Provider, DeviceOrPanel {
   private final DeviceProvider impl;
   private final InstanceContent content;
   private final String id;
   private final ChangeSupport changeSupport;
   final OutputSupport outputSupport;
   private Device.Status status;
   private boolean discovered;
   private String remoteName;
   private String modelName;
   private String serialNumber;
   private final Lookup lookup;

   Device(DeviceProvider var1, boolean var2, String var3) {
      this.status = Device.Status.USER_DISCONNECTED;
      this.changeSupport = new ChangeSupport(this);
      this.impl = var1;
      this.content = new InstanceContent();
      this.lookup = new AbstractLookup(this.content);
      this.id = var3;
      this.outputSupport = new OutputSupport(this, this::doOutputAction);
      var1.initialize(new Device.Callback());
      System.out.println("TRACE: device created: " + super.toString());
   }

   public void dispose() {
      this.disconnect();
      System.out.println("TRACE: device disposed: " + super.toString());
   }

   public Map<String, String> getSettings() {
      return this.impl.getSettings();
   }

   public boolean isConnected() {
      return this.status == Device.Status.SUCCESS;
   }

   public Device.Status getConnectionStatus() {
      return this.status;
   }

   public void disconnect() {
      if (this.isConnected()) {
         System.out.println("INFO: disconnecting: " + this + "(" + this.getId() + ")");
         this.status = Device.Status.USER_DISCONNECTED;
         this.impl.disconnect();
         System.out.println("INFO: " + this + "(" + this.getId() + ") disconnected.");
         this.fireChange();
      }

   }

   public Device.Status connect() {
      if (!this.isConnected()) {
         System.out.println("INFO: connecting: " + this + "(" + this.getId() + ")");
         this.status = this.impl.connect(true);

         assert this.status != Device.Status.USER_DISCONNECTED;

         if (this.status == Device.Status.SUCCESS) {
            this.discovered = true;
            System.out.println("INFO: " + this + "(" + this.getId() + ") connected.");
         } else {
            System.out.println("INFO: " + this + "(" + this.getId() + ") not connected: " + this.status);
         }

         this.fireChange();
         return this.status;
      } else {
         return Device.Status.SUCCESS;
      }
   }

   public boolean isDiscovered() {
      return this.discovered;
   }

   void setDiscovered(boolean var1) {
      this.discovered = var1;
   }

   public String getRemoteName() {
      return this.remoteName;
   }

   void setRemoteName(String var1) {
      this.remoteName = sanitize(var1);
   }

   public String getModelName() {
      return this.modelName;
   }

   void setModelName(String var1) {
      this.modelName = sanitize(var1);
   }

   public String getSerialNumber() {
      return this.serialNumber;
   }

   void setSerialNumber(String var1) {
      this.serialNumber = sanitize(var1);
   }

   public String getId() {
      return this.id;
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

   public Lookup getLookup() {
      return this.lookup;
   }

   public DeviceOrPanel getParent() {
      return this;
   }

   public int getNumber() {
      return -1;
   }

   public List<Output> getOutputs() {
      return ImmutableList.copyOf(this.outputSupport.getOutputs().values());
   }

   public Output getOutput(String var1) {
      return this.outputSupport.getOutput(var1);
   }

   private void doOutputAction(String var1, Output.Action var2) {
      OutputControlCookie var3 = (OutputControlCookie)this.getLookup().lookup(OutputControlCookie.class);
      if (var3 != null) {
         OutputControlProvider var4 = var3.getOutputControlProvider(var1);
         if (var4 != null) {
            System.out.println("DEBUG: output " + this + "/" + var1 + " doing action: " + var2);
            var4.doAction(var2);
            return;
         }
      }
      System.out.println("WARN: output " + this + "/" + var1 + ": OutputControlProvider not available");
   }

   static String sanitize(String var0) {
      return var0 != null && !var0.trim().isEmpty() ? var0.trim() : null;
   }

   private class Callback implements DeviceProvider.DeviceCallback {
      private Callback() {
      }

      public void connectionLost() {
         System.out.println("INFO: connection lost on " + Device.this + "(" + Device.this.getId() + ")");
         Device.this.disconnect();
      }

      public void changeOutputs(List<String> var1) {
         Device.this.outputSupport.changeOutputs(var1);
      }

      public void setOutputRemoteName(String var1, String var2) {
         Device.this.outputSupport.setOutputRemoteName(var1, var2);
      }

      public void setOutputEnabled(String var1, boolean var2) {
         Device.this.outputSupport.setOutputEnabled(var1, var2);
      }

      public void setRemoteName(String var1) {
         Device.this.setRemoteName(var1);
      }

      public void setModelName(String var1) {
         Device.this.setModelName(var1);
      }

      public void setSerialNumber(String var1) {
         Device.this.setSerialNumber(var1);
      }

   }

   public static enum Status {
      USER_DISCONNECTED,
      SUCCESS,
      INCOMPATIBLE,
      UNAUTHORIZED,
      UNREACHABLE;
   }
}
