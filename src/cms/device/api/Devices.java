package cms.device.api;

import cms.device.spi.DevicePlugin;
import cms.device.spi.DeviceProvider;
import cms.device.spi.PanelPlugin;
import cms.device.spi.PanelProvider;
import cms.device.spi.Plugin;
import cms.device.spi.ServerPlugin;
import cms.device.spi.ServerProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openide.util.Lookup;

public class Devices {
   private static final int ID_CHARS = 8;

   private Devices() {
   }

   public static List<DeviceType> getTypes() {
      List<DeviceType> var0 = new ArrayList(getPlugins().size());
      Iterator var1 = getPlugins().iterator();

      while(var1.hasNext()) {
         Plugin var2 = (Plugin)var1.next();
         var0.add(new DeviceType(var2));
      }

      Collections.sort(var0);
      return var0;
   }

   public static List<DeviceType> getAvailableTypes() {
      List<DeviceType> var0 = new ArrayList(getPlugins().size());
      Iterator var1 = getPlugins().iterator();

      while(var1.hasNext()) {
         Plugin var2 = (Plugin)var1.next();
         var0.add(new DeviceType(var2));
      }

      Collections.sort(var0);
      return var0;
   }

   public static boolean probeDevice(Map<String, String> var0) {
      DevicePlugin var1 = (DevicePlugin)getPlugin(var0.get("type"));
      DeviceProvider var2 = var1.newDevice(var0);
      var2.initialize(Callbacks.DeviceCb.DUMMY);
      boolean var3 = Device.Status.SUCCESS == var2.connect(false);
      if (var3) {
         var2.disconnect();
      }

      return var3;
   }

   public static boolean probeServer(Map<String, String> var0) {
      ServerPlugin var1 = (ServerPlugin)getPlugin(var0.get("type"));
      ServerProvider var2 = var1.newServer(var0);
      var2.initialize(Callbacks.ServerCb.DUMMY);
      boolean var3 = Device.Status.SUCCESS == var2.connect();
      if (var3) {
         var2.disconnect();
      }

      return var3;
   }

   public static boolean probePanel(Map<String, String> var0) {
      PanelPlugin var1 = (PanelPlugin)getPlugin(var0.get("type"));
      PanelProvider var2 = var1.newPanel(var0);
      var2.initialize(Callbacks.PanelCb.DUMMY);
      boolean var3 = Device.Status.SUCCESS == var2.connect();
      if (var3) {
         var2.disconnect();
      }

      return var3;
   }

   public static Connectable newConnectable(Map<String, String> var0) {
      return newConnectable(var0, (String)null);
   }

   public static Connectable newConnectable(Map<String, String> var0, String var1) {
      return newConnectable(getPlugin(var0.get("type")), var0, var1);
   }

   private static Connectable newConnectable(Plugin var0, Map<String, String> var1, String var2) {
      if (var0 instanceof DevicePlugin) {
         return newDevice((DevicePlugin)var0, var1, var2);
      } else {
         assert var0 instanceof PanelPlugin;

         return newPanel((PanelPlugin)var0, var1, var2);
      }
   }

   public static Device newDevice(Map<String, String> var0, String var1) {
      return newDevice((DevicePlugin)getPlugin(var0.get("type")), var0, var1);
   }

   private static Device newDevice(DevicePlugin var0, Map<String, String> var1, String var2) {
      String var3 = var2 != null && !var2.isEmpty() ? var2 : newId();
      return new Device(var0.newDevice(var1), isCool(var0), var3);
   }

   public static Panel newPanel(Map<String, String> var0, String var1) {
      return newPanel((PanelPlugin)getPlugin(var0.get("type")), var0, var1);
   }

   private static Panel newPanel(PanelPlugin var0, Map<String, String> var1, String var2) {
      return new Panel(var0.newPanel(var1));
   }

   private static Collection<? extends Plugin> getPlugins() {
      List<Plugin> var0 = new ArrayList();
      var0.addAll(Lookup.getDefault().lookupAll(DevicePlugin.class));
      var0.addAll(Lookup.getDefault().lookupAll(ServerPlugin.class));
      var0.addAll(Lookup.getDefault().lookupAll(PanelPlugin.class));
      return var0;
   }

   private static Plugin getPlugin(Object var0) {
      if (var0 instanceof DeviceType) {
         return ((DeviceType)var0).getPlugin();
      } else {
         Iterator var1 = getPlugins().iterator();

         Plugin var2;
         do {
            if (!var1.hasNext()) {
               throw new IllegalArgumentException(var0.toString());
            }

            var2 = (Plugin)var1.next();
         } while(!var2.getInternalName().equals(var0));

         return var2;
      }
   }

   private static boolean isCool(Plugin var0) {
      return var0.getInternalName().equals("platesLight") || var0.getInternalName().equals("xalFace");
   }

   public static String newId() {
      return new java.util.Random().ints(ID_CHARS, 0, 62)
         .mapToObj(i -> "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".charAt(i))
         .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
         .toString();
   }

}