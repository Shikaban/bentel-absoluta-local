package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscString;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class UserActivity extends DscCommandWithAppSeq {
   public static final int KEEP_ALIVE = 4;
   public static final int VALIDATE_USER_CODE = 22;
   private final DscVariableBytes partitionNumber = new DscVariableBytes();
   private final DscNumber type = DscNumber.newUnsignedNum(1);
   private final DscString userCode = DscString.newBCDString();

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.partitionNumber, this.type, this.userCode);
   }

   public int getCommandNumber() {
      return 2322;
   }

   public Integer getPartitionNumber() {
      return this.partitionNumber.toPositiveInteger();
   }

   public void setPartitionNumber(Integer var1) {
      this.partitionNumber.setPositiveInteger(var1);
   }

   public int getType() {
      return this.type.toInt();
   }

   public void setType(int var1) {
      this.type.set((long)var1);
   }

   public String getUserCode() {
      return this.userCode.toString();
   }

   public void setUserCode(String var1) {
      this.userCode.setString(var1);
   }
}
