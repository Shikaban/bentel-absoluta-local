package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public class TroubleDetail extends DscRequestableCommand {
   private final DscNumber deviceModuleType = DscNumber.newUnsignedNum(1);
   private final DscNumber troubleType = DscNumber.newUnsignedNum(1);
   private final DscBitMask deviceModuleNumbers = new DscBitMask(false, 1);

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.deviceModuleType, this.troubleType);
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.deviceModuleNumbers);
   }

   public int getCommandNumber() {
      return 2071;
   }

   public int getDeviceModuleType() {
      return this.deviceModuleType.toInt();
   }

   public void setDeviceModuleType(int var1) {
      this.deviceModuleType.set((long)var1);
   }

   public int getTroubleType() {
      return this.troubleType.toInt();
   }

   public void setTroubleType(int var1) {
      this.troubleType.set((long)var1);
   }

   public List<Integer> getDeviceModuleNumbers() {
      return this.deviceModuleNumbers.getTrueIndexes();
   }
}
