package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscStruct;
import protocol.dsc.base.DscVariableBytes;

import java.util.Collections;
import java.util.List;

public class ZoneAlarmStatus extends DscRequestableCommand implements DscArray.ElementProvider<ZoneAlarmStatus.ZoneAlarm> {
   private final DscVariableBytes partitionNumber = new DscVariableBytes();
   private final DscVariableBytes numberOfZones = new DscVariableBytes();
   private final DscArray<ZoneAlarmStatus.ZoneAlarm> alarms = new DscArray(this);

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.partitionNumber, this.numberOfZones);
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.alarms);
   }

   public int getCommandNumber() {
      return 2112;
   }

   public boolean match(DscRequestableCommand var1) {
      if (var1 instanceof ZoneAlarmStatus) {
         ZoneAlarmStatus var2 = (ZoneAlarmStatus)var1;
         return this.partitionNumber.equals(var2.partitionNumber);
      } else {
         return false;
      }
   }

   public int getPartitionNumber() {
      return this.partitionNumber.toPositiveInt();
   }

   public void setPartitionNumber(int var1) {
      this.partitionNumber.setPositiveInt(var1);
   }

   public int getNumberOfZones() {
      return this.numberOfZones.toPositiveInt();
   }

   public void setRequestsAllZones() {
      this.numberOfZones.setPositiveInt(0, 1);
   }

   public List<ZoneAlarmStatus.ZoneAlarm> getAlarms() {
      return Collections.unmodifiableList(this.alarms);
   }

   public int numberOfElements() {
      return this.getNumberOfZones();
   }

   public ZoneAlarmStatus.ZoneAlarm newElement() {
      return new ZoneAlarmStatus.ZoneAlarm();
   }

   public static class ZoneAlarm extends DscStruct {
      private final DscVariableBytes zoneNumber = new DscVariableBytes();
      private final DscNumber typeOfAlarm = DscNumber.newUnsignedNum(1);
      private final DscBitMask alarmState = new DscBitMask(1, 0);

      protected List<DscSerializable> getFields() {
         return ImmutableList.of(this.zoneNumber, this.typeOfAlarm, this.alarmState);
      }

      public int getZoneNumber() {
         return this.zoneNumber.toPositiveInt();
      }

      public int getTypeOfAlarm() {
         return this.typeOfAlarm.toInt();
      }

      public boolean getAlarmState() {
         return this.alarmState.get(0);
      }
   }
}
