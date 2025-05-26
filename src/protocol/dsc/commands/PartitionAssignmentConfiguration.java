package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public class PartitionAssignmentConfiguration extends DscRequestableCommand {
   private final DscBitMask partitions = new DscBitMask(true, 1);

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of();
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.partitions);
   }

   public int getCommandNumber() {
      return 1906;
   }

   public List<Integer> getAssignedPartitions() {
      return this.partitions.getTrueIndexes();
   }
}
