package plugin.absoluta.connection;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class CustomizedArmingModes {
   private static final int CUSTOMIZED_ARMING_MODE_A = 13;
   private static final int CUSTOMIZED_ARMING_MODE_B = 14;
   private static final int CUSTOMIZED_ARMING_MODE_C = 15;
   private static final int CUSTOMIZED_ARMING_MODE_D = 16;
   static final Map<Character, Integer> CUSTOMIZED_ARMING_MODES = ImmutableMap.<Character, Integer>builder()
      .put('A', CUSTOMIZED_ARMING_MODE_A)
      .put('B', CUSTOMIZED_ARMING_MODE_B)
      .put('C', CUSTOMIZED_ARMING_MODE_C)
      .put('D', CUSTOMIZED_ARMING_MODE_D)
      .build();
   public static final Map<Integer, Character> ARMING_MODE_LABELS = ImmutableMap.<Integer, Character>builder()
      .put(1, 'A')
      .put(2, 'B')
      .put(3, 'C')
      .put(4, 'D')
      .build();

   private CustomizedArmingModes() {
   }
}
