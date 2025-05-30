package cms.device.spi;

import java.util.List;
import java.util.Map;

import cms.device.api.Device;

public interface DeviceProvider {
   void initialize(DeviceProvider.DeviceCallback var1);

   Map<String, String> getSettings();

   Device.Status connect(boolean var1);

   void disconnect();

   public interface DeviceCallback extends ConnectionListener {

      void changeOutputs(List<String> var1);

      void setOutputRemoteName(String var1, String var2);

      void setOutputEnabled(String var1, boolean var2);

      void setRemoteName(String var1);

   }
}
