package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscCharsets;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.nio.charset.Charset;
import java.util.List;

public class EnterConfigurationMode extends DscCommandWithAppSeq {
   public static final int INSTALLERS_PROGRAMMING = 0;
   public static final int ACCESS_CODE_PROGRAMMING = 1;
   public static final int USER_FUNCTION_PROGRAMMING = 2;
   public static final int USER_BYPASS_PROGRAMMING = 3;
   private static final Charset ACCESS_CODE_CHARSET;
   private final DscVariableBytes partitionNumber = new DscVariableBytes();
   private final DscNumber type = DscNumber.newUnsignedNum(1);
   private final DscVariableBytes programmingAccessCode = new DscVariableBytes();
   private final DscBitMask readWriteAccess = new DscBitMask(1, 0);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.partitionNumber, this.type, this.programmingAccessCode, this.readWriteAccess);
   }

   public int getCommandNumber() {
      return 1796;
   }

   public int getPartitionNumber() {
      return this.partitionNumber.toPositiveInt();
   }

   public void setPartitionNumber(int var1) {
      this.partitionNumber.setPositiveInt(var1);
   }

   public int getType() {
      return this.type.toInt();
   }

   public void setType(int var1) {
      this.type.set((long)var1);
   }

   public String getProgrammingAccessCode() {
      return this.programmingAccessCode.toString(ACCESS_CODE_CHARSET);
   }

   public void setProgrammingAccessCode(String var1) {
      this.programmingAccessCode.setString(ACCESS_CODE_CHARSET, var1);
   }

   public boolean isReadWrite() {
      return this.readWriteAccess.get(0);
   }

   public void setReadWrite(boolean var1) {
      this.readWriteAccess.set(0, (Boolean)var1);
   }

   static {
      ACCESS_CODE_CHARSET = DscCharsets.BCD;
   }
}
