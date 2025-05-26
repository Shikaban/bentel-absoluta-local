package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscString;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class PartitionArmControl extends DscCommandWithAppSeq {
   private final DscVariableBytes partitionNumber = new DscVariableBytes();
   private final DscNumber armMode = DscNumber.newUnsignedNum(1);
   private final DscString userCode = DscString.newBCDString();

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.partitionNumber, this.armMode, this.userCode);
   }

   public int getCommandNumber() {
      return 2304;
   }

   public Integer getPartitionNumber() {
      return this.partitionNumber.toPositiveInteger();
   }

   public void setPartitionNumber(Integer var1) {
      this.partitionNumber.setPositiveInteger(var1);
   }

   public int getArmMode() {
      return this.armMode.toInt();
   }

   public void setArmMode(int var1) {
      this.armMode.set((long)var1);
   }

   public String getUserCode() {
      return this.userCode.toString();
   }

   public void setUserCode(String var1) {
      this.userCode.setString(var1);
   }
}
