package cms.device.spi;

import java.util.Map;

public interface DevicePlugin extends Plugin {
   DeviceProvider newDevice(Map<String, String> var1);
}
