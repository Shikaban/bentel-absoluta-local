package cms.device.spi;

import java.util.Map;

public interface PanelPlugin extends Plugin {
   PanelProvider newPanel(Map<String, String> var1);

   Map<String, String> getPluginValidation();
}
