package protocol.dsc.messages;

import com.google.common.collect.ImmutableList;

import protocol.dsc.commands.SectionReadResponse;

import java.util.ArrayList;
import java.util.List;
import org.javatuples.Pair;

public class AbsolutaEnabledOutputsAndRemoteCommandsReading extends SectionReading<Pair<List<Integer>, List<Integer>>> {
   public AbsolutaEnabledOutputsAndRemoteCommandsReading() {
      super(1, ImmutableList.of());
   }

   protected Pair<List<Integer>, List<Integer>> getResponseValue(SectionReadResponse var1) {
      List<Boolean> var2 = var1.getDataAsBitMask(0);
      List<Integer> var3 = new ArrayList(50);
      List<Integer> var4 = new ArrayList(32);

      for(int var5 = 0; var5 < var2.size(); ++var5) {
         if ((Boolean)var2.get(var5)) {
            if (var5 < 50) {
               var3.add(var5 + 1);
            } else if (56 <= var5 && var5 < 88) {
               var4.add(var5 - 55);
            }
         }
      }

      return Pair.with(var3, var4);
   }
}
