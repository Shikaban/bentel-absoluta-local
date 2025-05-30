package cms.device.spi;

import java.util.Map;

public interface PanelPlugin  {
   PanelProvider newPanel(Map<String, String> var1);
}
