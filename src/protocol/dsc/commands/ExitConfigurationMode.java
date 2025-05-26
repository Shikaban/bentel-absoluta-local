package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class ExitConfigurationMode extends DscCommandWithAppSeq {
   private final DscVariableBytes partitionNumber = new DscVariableBytes();

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.partitionNumber);
   }

   public int getCommandNumber() {
      return 1793;
   }

   public int getPartitionNumber() {
      return this.partitionNumber.toPositiveInt();
   }

   public void setPartitionNumber(int var1) {
      this.partitionNumber.setPositiveInt(var1);
   }
}
