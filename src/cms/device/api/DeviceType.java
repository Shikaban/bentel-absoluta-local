package cms.device.api;

import cms.device.spi.Plugin;

public final class DeviceType implements Comparable<DeviceType> {
   private final Plugin plugin;

   DeviceType(Plugin var1) {
      this.plugin = var1;
   }

   Plugin getPlugin() {
      return this.plugin;
   }

   public String getInternalName() {
      return this.plugin.getInternalName();
   }

   public String getLocalizedName() {
      return this.plugin.getLocalizedName();
   }

   public String toString() {
      return this.plugin.getLocalizedName();
   }

   public int compareTo(DeviceType var1) {
      return this.toString().compareToIgnoreCase(var1.toString());
   }

   public boolean equals(Object var1) {
      return var1 instanceof DeviceType && this.plugin == ((DeviceType)var1).plugin;
   }

   public int hashCode() {
      return this.plugin.hashCode();
   }
}
