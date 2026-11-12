package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscString;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class PartitionDisarmControl extends DscCommandWithAppSeq {
   private final DscVariableBytes partitionNumber = new DscVariableBytes();
   private final DscString userCode = DscString.newBCDString();

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.partitionNumber, this.userCode);
   }

   public int getCommandNumber() {
      return 2305;
   }

   public Integer getPartitionNumber() {
      return this.partitionNumber.toPositiveInteger();
   }

   public void setPartitionNumber(Integer positiveInteger) {
      this.partitionNumber.setPositiveInteger(positiveInteger);
   }

   public String getUserCode() {
      return this.userCode.toString();
   }

   public void setUserCode(String text) {
      this.userCode.setString(text);
   }
}
