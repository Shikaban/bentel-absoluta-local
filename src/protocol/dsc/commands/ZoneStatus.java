package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.Collections;
import java.util.List;

public class ZoneStatus extends DscRequestableCommand implements DscArray.ElementProvider<DscBitMask> {
   private final DscVariableBytes zoneNumber = new DscVariableBytes();
   private final DscVariableBytes numberOfZones = new DscVariableBytes();
   private final DscNumber lengthOfStatusBytes = DscNumber.newUnsignedNum(1);
   private final DscArray<DscBitMask> zoneStatuses = new DscArray<DscBitMask>(this);

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.zoneNumber, this.numberOfZones);
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.lengthOfStatusBytes, this.zoneStatuses);
   }

   public int getCommandNumber() {
      return 2065;
   }

   public boolean match(DscRequestableCommand requestableCommand) {
      if (!(requestableCommand instanceof ZoneStatus)) {
         return false;
      } else {
         ZoneStatus zoneStatus2 = (ZoneStatus)requestableCommand;
         int requestedStart = this.getZoneNumber();
         int requestedEnd = requestedStart + this.getNumberOfZones();
         int responseStart = zoneStatus2.getZoneNumber();
         int responseEnd = responseStart + zoneStatus2.getNumberOfZones();
         return responseStart <= requestedStart && requestedEnd <= responseEnd;
      }
   }

   public int getZoneNumber() {
      return this.zoneNumber.toPositiveInt();
   }

   public void setZoneNumber(int value) {
      this.zoneNumber.setPositiveInt(value);
   }

   public int getNumberOfZones() {
      return this.numberOfZones.toPositiveInt();
   }

   public void setNumberOfZones(int value) {
      this.numberOfZones.setPositiveInt(value);
   }

   public int getLengthOfStatusBytes() {
      return this.lengthOfStatusBytes.toInt();
   }

   public List<DscBitMask> getZoneStatuses() {
      return Collections.unmodifiableList(this.zoneStatuses);
   }

   public int numberOfElements() {
      return this.getNumberOfZones();
   }

   public DscBitMask newElement() {
      return new DscBitMask(this.getLengthOfStatusBytes(), 0);
   }
}
