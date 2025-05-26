package cms.device.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

import cms.device.spi.DeviceProvider;
import cms.device.spi.ServerProvider;

public final class Server implements Connectable, Model {
   private final ServerProvider impl;
   private final String id;
   private final Map<String, Device> children;
   private final ChangeSupport changeSupport;
   private String localName;
   private String remoteName;
   private boolean connected;
   private boolean discovered;

   Server(ServerProvider var1, String var2) {
      assert var2 != null && !var2.isEmpty();

      this.impl = var1;
      this.id = var2;
      this.children = new LinkedHashMap();
      var1.initialize(new Server.Callback());
      this.changeSupport = new ChangeSupport(this);
   }

   public Map<String, String> getSettings() {
      return this.impl.getSettings();
   }

   public Device.Status connect() {
      if (this.connected) {
         return Device.Status.SUCCESS;
      } else {
         Device.Status var1 = this.impl.connect();
         if (var1 != Device.Status.SUCCESS) {
            return var1;
         } else {
            Iterator var2 = this.children.values().iterator();

            while(var2.hasNext()) {
               Device var3 = (Device)var2.next();
               var3.connect();
            }

            this.connected = true;
            this.discovered = true;
            this.changeSupport.fireChange();
            return Device.Status.SUCCESS;
         }
      }
   }

   void doSetChildrenKeys(List<String> var1, Map<String, String> var2) {
      Map<String, Device> var3 = new LinkedHashMap();
      Iterator var4 = var1.iterator();

      while(var4.hasNext()) {
         String var5 = (String)var4.next();
         if (this.children.containsKey(var5)) {
            var3.put(var5, this.children.remove(var5));
         } else {
            DeviceProvider var6 = this.impl.newDevice(var5);
            String var7 = var2 != null ? (String)var2.get(var5) : Devices.newId();
            Device var8 = new Device(var6, false, var7, this);
            var3.put(var5, var8);
         }
      }

      var4 = this.children.values().iterator();

      while(var4.hasNext()) {
         Device var9 = (Device)var4.next();
         var9.dispose();
      }

      this.children.clear();
      this.children.putAll(var3);
   }

   private void doSetChildConnected(String var1, boolean var2) {
      Device var3 = (Device)this.children.get(var1);
      if (var3 != null) {
         if (var2) {
            var3.connect();
         } else {
            var3.disconnect();
         }
      }

   }

   public void disconnect() {
      if (this.connected) {
         Iterator var1 = this.children.values().iterator();

         while(var1.hasNext()) {
            Device var2 = (Device)var1.next();
            var2.disconnect();
         }

         this.impl.disconnect();
         this.connected = false;
         this.changeSupport.fireChange();
      }

   }

   public boolean isConnected() {
      return this.connected;
   }

   public boolean isUserConnectable() {
      return true;
   }

   public void dispose() {
      this.disconnect();
      Iterator var1 = this.children.values().iterator();

      while(var1.hasNext()) {
         Device var2 = (Device)var1.next();
         var2.dispose();
      }

   }

   public boolean isDiscovered() {
      return this.discovered;
   }

   void setDiscovered(boolean var1) {
      this.discovered = var1;
   }

   public List<Device> getDevices() {
      return Collections.unmodifiableList(new ArrayList(this.children.values()));
   }

   Map<String, Device> getDevicesMap() {
      return Collections.unmodifiableMap(this.children);
   }

   public String getLocalName() {
      return this.localName;
   }

   public void setLocalName(String var1) {
      String var2 = Device.sanitize(var1);
      if (!Objects.equals(this.localName, var2)) {
         this.localName = var2;
         this.changeSupport.fireChange();
      }

   }

   public String getRemoteName() {
      return this.remoteName;
   }

   void setRemoteName(String var1) {
      this.remoteName = Device.sanitize(var1);
   }

   public String getName() {
      if (this.getLocalName() != null) {
         return this.getLocalName();
      } else {
         return this.getRemoteName() != null ? this.getRemoteName() : this.impl.getDefaultName();
      }
   }

   public String getId() {
      return this.id;
   }

   public String toString() {
      return this.getName();
   }

   public void addChangeListener(ChangeListener var1) {
      this.changeSupport.addChangeListener(var1);
   }

   public void removeChangeListener(ChangeListener var1) {
      this.changeSupport.removeChangeListener(var1);
   }

   private class Callback implements ServerProvider.ServerCallback {
      private Callback() {
      }

      public void connectionLost() {
         Server.this.disconnect();
      }

      public void setChildrenKeys(List<String> var1) {
         Server.this.doSetChildrenKeys(var1, (Map)null);
      }

      public void setRemoteName(String var1) {
         Server.this.setRemoteName(var1);
      }

      public void setChildConnected(String var1, boolean var2) {
         Server.this.doSetChildConnected(var1, var2);
      }

   }
}
