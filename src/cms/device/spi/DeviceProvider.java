package cms.device.spi;

import java.util.Map;

import cms.device.api.Device;

public interface DeviceProvider {
   void initialize(DeviceProvider.DeviceCallback var1);

   Map<String, String> getSettings();

   Device.Status connect(boolean var1);

   void disconnect();

   public interface DeviceCallback extends ConnectionListener {

      void setRemoteName(String var1);

   }
}
