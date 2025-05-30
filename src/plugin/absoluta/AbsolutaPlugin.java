package plugin.absoluta;

import cms.device.spi.PanelPlugin;
import cms.device.spi.PanelProvider;

import java.util.Map;

public class AbsolutaPlugin implements PanelPlugin {
   public PanelProvider newPanel(Map<String, String> var1) {
      return new AbsolutaPanelProvider(var1);
   }
}
