package cms.device.util;

import cms.device.api.PluginValidator;

public interface ParamValidator {
   PluginValidator.Result validate(String var1, Object var2);
}
