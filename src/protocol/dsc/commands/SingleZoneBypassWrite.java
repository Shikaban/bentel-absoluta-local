package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class SingleZoneBypassWrite extends DscCommandWithAppSeq {
   private final DscVariableBytes partitionNumber = new DscVariableBytes();
   private final DscVariableBytes zoneNumber = new DscVariableBytes();
   private final DscBitMask bypassState = new DscBitMask(1, 0);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.partitionNumber, this.zoneNumber, this.bypassState);
   }

   public int getCommandNumber() {
      return 1866;
   }

   public Integer getPartitionNumber() {
      return this.partitionNumber.toPositiveInteger();
   }

   public void setPartitionNumber(Integer positiveInteger) {
      this.partitionNumber.setPositiveInteger(positiveInteger);
   }

   public int getZoneNumber() {
      return this.zoneNumber.toPositiveInt();
   }

   public void setZoneNumber(int value) {
      this.zoneNumber.setPositiveInt(value);
   }

   public boolean isBypass() {
      return this.bypassState.get(0);
   }

   public void setBypass(boolean enabled) {
      this.bypassState.set(0, (Boolean)enabled);
   }
}
