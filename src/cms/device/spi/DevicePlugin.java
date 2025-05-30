package cms.device.spi;

import java.util.Map;

public interface DevicePlugin  {
   DeviceProvider newDevice(Map<String, String> var1);
}
