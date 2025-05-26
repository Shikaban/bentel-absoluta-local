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
   private final DscArray<DscBitMask> zoneStatuses = new DscArray(this);

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.zoneNumber, this.numberOfZones);
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.lengthOfStatusBytes, this.zoneStatuses);
   }

   public int getCommandNumber() {
      return 2065;
   }

   public boolean match(DscRequestableCommand var1) {
      if (!(var1 instanceof ZoneStatus)) {
         return false;
      } else {
         ZoneStatus var2 = (ZoneStatus)var1;
         int var3 = this.getZoneNumber();
         int var4 = this.getZoneNumber();
         int var5 = var2.getZoneNumber();
         int var6 = var2.getZoneNumber();
         return var5 <= var3 && var3 + var4 <= var5 + var6;
      }
   }

   public int getZoneNumber() {
      return this.zoneNumber.toPositiveInt();
   }

   public void setZoneNumber(int var1) {
      this.zoneNumber.setPositiveInt(var1);
   }

   public int getNumberOfZones() {
      return this.numberOfZones.toPositiveInt();
   }

   public void setNumberOfZones(int var1) {
      this.numberOfZones.setPositiveInt(var1);
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
