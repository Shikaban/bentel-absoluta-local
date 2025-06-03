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

   @Override
   protected Pair<List<Integer>, List<Integer>> getResponseValue(SectionReadResponse response) {
      List<Boolean> bitMask = response.getDataAsBitMask(0);
      List<Integer> enabledOutputs = new ArrayList<>(50);
      List<Integer> enabledRemoteCommands = new ArrayList<>(32);

      for (int i = 0; i < bitMask.size(); i++) {
         if (Boolean.TRUE.equals(bitMask.get(i))) {
               if (i < 50) {
                  enabledOutputs.add(i + 1);
               } else if (i >= 56 && i < 88) {
                  enabledRemoteCommands.add(i - 55);
               }
         }
      }

      return Pair.with(enabledOutputs, enabledRemoteCommands);
   }
}
