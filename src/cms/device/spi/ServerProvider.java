package cms.device.spi;

import java.util.List;
import java.util.Map;

import cms.device.api.Device;

public interface ServerProvider {
   void initialize(ServerProvider.ServerCallback var1);

   Map<String, String> getSettings();

   Device.Status connect();

   void disconnect();

   String getDefaultName();

   DeviceProvider newDevice(String var1);

   public interface ServerCallback extends ConnectionListener {
      void setChildrenKeys(List<String> var1);

      void setRemoteName(String var1);

      void setChildConnected(String var1, boolean var2);
   }
}
