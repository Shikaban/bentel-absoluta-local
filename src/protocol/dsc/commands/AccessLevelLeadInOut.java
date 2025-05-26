package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class AccessLevelLeadInOut extends DscAbstractCommand {
   private final DscVariableBytes partitionNumber = new DscVariableBytes();
   private final DscNumber type = DscNumber.newUnsignedNum(1);
   private final DscVariableBytes user = new DscVariableBytes();
   private final DscNumber access = DscNumber.newUnsignedNum(1);
   private final DscNumber mode = DscNumber.newUnsignedNum(1);
   private final DscNumber date = DscNumber.newUnsignedNum(4);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.partitionNumber, this.type, this.user, this.access, this.mode, this.date);
   }

   public int getCommandNumber() {
      return 1026;
   }

   public Integer getPartitionNumber() {
      return this.partitionNumber.toPositiveInteger();
   }

   public int getType() {
      return this.type.toInt();
   }

   public int getUser() {
      return this.user.toPositiveInt();
   }

   public int getAccess() {
      return this.type.toInt();
   }

   public int getMode() {
      return this.type.toInt();
   }
}
