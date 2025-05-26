package cms.device.spi;

import java.util.Map;

public interface DevicePlugin2 extends DevicePlugin {
   Map<String, String> getPluginValidation();
}
