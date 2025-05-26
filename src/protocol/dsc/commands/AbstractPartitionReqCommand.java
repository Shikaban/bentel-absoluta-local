package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public abstract class AbstractPartitionReqCommand extends DscRequestableCommand {
   private final DscVariableBytes partitionNumber = new DscVariableBytes();

   protected final List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.partitionNumber);
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of();
   }

   public final Integer getPartitionNumber() {
      return this.partitionNumber.toPositiveInteger();
   }

   public final void setPartitionNumber(Integer var1) {
      this.partitionNumber.setPositiveInteger(var1);
   }
}
