package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public class AccessCodeLengthNotification extends DscRequestableCommand {
   private final DscNumber length = DscNumber.newUnsignedNum(1);

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of();
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.length);
   }

   public int getCommandNumber() {
      return 566;
   }

   public int getLength() {
      return this.length.toInt();
   }
}
