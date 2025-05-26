package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public class ZoneAssignmentConfiguration extends AbstractPartitionReqCommand {
   private final DscBitMask partitionAssignment = new DscBitMask(false, 1);

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.partitionAssignment);
   }

   public int getCommandNumber() {
      return 1904;
   }

   public List<Integer> getAssignedZones() {
      return this.partitionAssignment.getTrueIndexes();
   }
}
