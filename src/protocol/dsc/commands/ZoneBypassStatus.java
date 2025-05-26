package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class ZoneBypassStatus extends DscRequestableCommand {
   private final DscVariableBytes partitionNumber = new DscVariableBytes();
   private final DscBitMask bypassInfo = new DscBitMask(false, 1);

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.partitionNumber);
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.bypassInfo);
   }

   public int getCommandNumber() {
      return 2067;
   }

   public int getPartitionNumber() {
      return this.partitionNumber.toPositiveInt();
   }

   public void setPartitionNumber(int var1) {
      this.partitionNumber.setPositiveInt(var1);
   }

   public void setGlobalPartition() {
      this.partitionNumber.setPositiveInt(0, 1);
   }

   public List<Integer> getBypassedZones() {
      return this.bypassInfo.getTrueIndexes();
   }
}
