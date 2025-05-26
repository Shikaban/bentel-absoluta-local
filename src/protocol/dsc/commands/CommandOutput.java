package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscString;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class CommandOutput extends DscCommandWithAppSeq {
   private final DscVariableBytes partitionNumber = new DscVariableBytes();
   private final DscVariableBytes outputNumber = new DscVariableBytes();
   private final DscNumber activationType = DscNumber.newUnsignedNum(1);
   private final DscString userCode = DscString.newBCDString();

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.partitionNumber, this.outputNumber, this.activationType, this.userCode);
   }

   public int getCommandNumber() {
      return 2306;
   }

   public Integer getPartitionNumber() {
      return this.partitionNumber.toPositiveInteger();
   }

   public void setPartitionNumber(Integer var1) {
      this.partitionNumber.setPositiveInteger(var1);
   }

   public Integer getOutputNumber() {
      return this.outputNumber.toPositiveInteger();
   }

   public void setOutputNumber(Integer var1) {
      this.outputNumber.setPositiveInteger(var1);
   }

   public int getActivationType() {
      return this.activationType.toInt();
   }

   public void setActivationType(int var1) {
      this.activationType.set((long)var1);
   }

   public String getUserCode() {
      return this.userCode.toString();
   }

   public void setUserCode(String var1) {
      this.userCode.setString(var1);
   }
}
