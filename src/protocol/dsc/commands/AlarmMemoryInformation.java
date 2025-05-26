package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public class AlarmMemoryInformation extends AbstractPartitionReqCommand {
   private final DscBitMask miscellaneousAlarms = new DscBitMask(1, 0);
   private final DscBitMask zoneAlarms = new DscBitMask(false, 1);

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.miscellaneousAlarms, this.zoneAlarms);
   }

   public int getCommandNumber() {
      return 2069;
   }

   public boolean getCOAlarm() {
      return this.miscellaneousAlarms.get(1);
   }

   public boolean getFireAlarm() {
      return this.miscellaneousAlarms.get(0);
   }

   public List<Integer> getZoneAlarms() {
      return this.zoneAlarms.getTrueIndexes();
   }
}
