package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;

import java.util.Collections;
import java.util.List;

public class EventReportingConfigurationWrite extends DscCommandWithAppSeq {
   private final DscBitMask reportingType = new DscBitMask(1, 0);
   private final DscNumber eventType = DscNumber.newUnsignedNum(1);
   private final DscBitMask settings = new DscBitMask(true, 0);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.reportingType, this.eventType, this.settings);
   }

   public int getCommandNumber() {
      return 1890;
   }

   public boolean getReportingType(int var1) {
      return this.reportingType.get(var1);
   }

   public void setReportingType(int var1, boolean var2) {
      this.reportingType.set(var1, var2);
   }

   public int getEventType() {
      return this.eventType.toInt();
   }

   public void setEventType(int var1) {
      this.eventType.set((long)var1);
   }

   public List<Boolean> getSettings() {
      return Collections.unmodifiableList(this.settings);
   }

   public void setSettings(List<Boolean> var1) {
      int var2 = var1.size();
      this.settings.setMinNumberOfBits(var2);

      for(int var3 = 0; var3 < var2; ++var3) {
         this.settings.set(var3, (Boolean)var1.get(var3));
      }

   }
}
