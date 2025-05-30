package cms.device.api;

public final class DeviceType implements Comparable<DeviceType> {

   public int compareTo(DeviceType var1) {
      return this.toString().compareToIgnoreCase(var1.toString());
   }

}
