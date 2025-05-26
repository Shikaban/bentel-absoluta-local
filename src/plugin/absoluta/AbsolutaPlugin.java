package plugin.absoluta;

import com.google.common.collect.ImmutableMap;

import cms.device.spi.PanelPlugin;
import cms.device.spi.PanelProvider;

import java.util.Map;
import org.openide.util.NbBundle;

public class AbsolutaPlugin implements PanelPlugin {
   static final String NAME = "absoluta";

   public String getInternalName() {
      return "absoluta";
   }

   public String getLocalizedName() {
      return NbBundle.getMessage(AbsolutaPlugin.class, "PluginName");
   }

   public PanelProvider newPanel(Map<String, String> var1) {
      return new AbsolutaPanelProvider(var1);
   }

   public Map<String, String> getPluginValidation() {
      return ImmutableMap.<String, String>builder()
         .put("address", "address")
         .put("port", "port")
         .put("pin", "pin")
         .build();
   }
}
