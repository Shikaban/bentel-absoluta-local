package cms.device.util;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class AddressValidator {
   private static final Pattern ip4regex = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

   public boolean isValidAddress(String var1) {
      return this.isValidIPv4(var1);
   }

   public boolean isValidIPv4(String var1) {
      if (var1 == null) {
         return false;
      } else if (!ip4regex.matcher(var1).matches()) {
         return false;
      } else {
         String[] var2 = StringUtils.split(var1, '.');
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String var5 = var2[var4];
            if (Integer.parseInt(var5) > 255) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isValidPort(String var1) {
      int var2;
      try {
         var2 = Integer.parseInt(var1);
      } catch (NumberFormatException | NullPointerException e) {
         var2 = -1;
      }

      return var2 >= 0 && var2 <= 65535;
   }

}
