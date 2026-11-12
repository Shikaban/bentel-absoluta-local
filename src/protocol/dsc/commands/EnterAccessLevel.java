package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscCharsets;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.nio.charset.Charset;
import java.util.List;

public class EnterAccessLevel extends DscCommandWithAppSeq {
   public static final int INSTALLER_ACCESS_LEVEL = 0;
   public static final int ENHANCED_INSTALLER_ACCESS_LEVEL = 1;
   public static final int USER_ACCESS_LEVEL = 2;
   private static final Charset ACCESS_CODE_CHARSET;
   private final DscVariableBytes partitionNumber = new DscVariableBytes();
   private final DscNumber type = DscNumber.newUnsignedNum(1);
   private final DscVariableBytes programmingAccessCode = new DscVariableBytes();

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.partitionNumber, this.type, this.programmingAccessCode);
   }

   public int getCommandNumber() {
      return 1024;
   }

   public Integer getPartitionNumber() {
      return this.partitionNumber.toPositiveInteger();
   }

   public void setPartitionNumber(Integer positiveInteger) {
      this.partitionNumber.setPositiveInteger(positiveInteger);
   }

   public int getType() {
      return this.type.toInt();
   }

   public void setType(int value) {
      this.type.set((long)value);
   }

   public String getProgrammingAccessCode() {
      return this.programmingAccessCode.toString(ACCESS_CODE_CHARSET);
   }

   public void setProgrammingAccessCode(String text) {
      this.programmingAccessCode.setString(ACCESS_CODE_CHARSET, text);
   }

   static {
      ACCESS_CODE_CHARSET = DscCharsets.BCD;
   }
}
