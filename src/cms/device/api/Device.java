package cms.device.api;

import cms.device.spi.DeviceProvider;
import java.util.Map;
import org.openide.util.ChangeSupport;

public final class Device implements DeviceOrPanel {
   private final DeviceProvider impl;
   private final String id;
   private final ChangeSupport changeSupport;
   private Panel.connStatus status;
   private boolean discovered;
   private String remoteName;

   Device(DeviceProvider var1, boolean var2, String var3) {
      this.status = Panel.connStatus.USER_DISCONNECTED;
      this.changeSupport = new ChangeSupport(this);
      this.impl = var1;
      this.id = var3;
      var1.initialize(new Device.Callback());
      System.out.println("TRACE: device created: " + super.toString());
   }

   public Map<String, String> getSettings() {
      return this.impl.getSettings();
   }

   public boolean isConnected() {
      return this.status == Panel.connStatus.SUCCESS;
   }

   public Panel.connStatus getConnectionStatus() {
      return this.status;
   }

   public void disconnect() {
      if (this.isConnected()) {
         System.out.println("INFO: disconnecting: " + this + "(" + this.id + ")");
         this.status = Panel.connStatus.USER_DISCONNECTED;
         this.impl.disconnect();
         System.out.println("INFO: " + this + "(" + this.id + ") disconnected.");
         this.fireChange();
      }

   }

   public Panel.connStatus connect() {
      if (!this.isConnected()) {
         System.out.println("INFO: connecting: " + this + "(" + this.id + ")");
         this.status = this.impl.connect(true);

         assert this.status != Panel.connStatus.USER_DISCONNECTED;

         if (this.status == Panel.connStatus.SUCCESS) {
            this.discovered = true;
            System.out.println("INFO: " + this + "(" + this.id + ") connected.");
         } else {
            System.out.println("INFO: " + this + "(" + this.id + ") not connected: " + this.status);
         }

         this.fireChange();
         return this.status;
      } else {
         return Panel.connStatus.SUCCESS;
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

   public void fireChange() {
      this.changeSupport.fireChange();
   }

   static String sanitize(String var0) {
      return var0 != null && !var0.trim().isEmpty() ? var0.trim() : null;
   }

   private class Callback implements DeviceProvider.DeviceCallback {
      private Callback() {
      }

      public void connectionLost() {
         System.out.println("INFO: connection lost on " + Device.this + "(" + Device.this.id + ")");
         Device.this.disconnect();
      }

      public void setRemoteName(String var1) {
         Device.this.setRemoteName(var1);
      }

   }
}
