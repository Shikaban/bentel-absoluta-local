package cms.device.spi;

import java.util.Map;

import cms.device.api.Panel;

public interface DeviceProvider {
   void initialize(DeviceProvider.DeviceCallback var1);

   Map<String, String> getSettings();

   Panel.connStatus connect(boolean var1);

   void disconnect();

   public interface DeviceCallback extends ConnectionListener {

      void setRemoteName(String var1);

   }
}
