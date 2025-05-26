package cms.device.spi;

import java.util.Map;

public interface ServerPlugin extends Plugin {
   ServerProvider newServer(Map<String, String> var1);

   Map<String, String> getPluginValidation();
}
