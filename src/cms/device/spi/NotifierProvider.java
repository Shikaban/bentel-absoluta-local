package cms.device.spi;

import java.util.Date;
import java.util.Map;

public interface NotifierProvider {
   void initialize(NotifierProvider.NotifierCallback var1);

   void start();

   void stop();

   public interface NotifierCallback {
      void fire(SourceType var1, int var2, Date var3, String var4, Map<String, String> var5);

      void fire(SourceType var1, String var2, Date var3, String var4, Map<String, String> var5);

      void fire(Date var1, String var2, Map<String, String> var3);
   }
}
