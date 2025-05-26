package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public class CommandOutputActivation extends AbstractPartitionReqCommand {
   private final DscBitMask outputBitmask = new DscBitMask(true, 1);

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.outputBitmask);
   }

   public int getCommandNumber() {
      return 546;
   }

   public List<Integer> getActiveOutputs() {
      return this.outputBitmask.getTrueIndexes();
   }
}
