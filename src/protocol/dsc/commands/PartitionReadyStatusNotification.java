package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public class PartitionReadyStatusNotification extends AbstractPartitionReqCommand {
   public static final int RESERVED = 0;
   public static final int READY_TO_ARM = 1;
   public static final int READY_TO_FORCE_ARM = 2;
   public static final int NOT_READY_TO_ARM = 3;

   private final DscNumber status = DscNumber.newUnsignedNum(1);

   public int getCommandNumber() {
      return 569;
   }

   @Override
   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.status);
   }

   public int getStatus() {
      return this.status.toInt();
   }
}
