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

   public boolean getReportingType(int numericValue) {
      return this.reportingType.get(numericValue);
   }

   public void setReportingType(int value, boolean enabled) {
      this.reportingType.set(value, enabled);
   }

   public int getEventType() {
      return this.eventType.toInt();
   }

   public void setEventType(int value) {
      this.eventType.set((long)value);
   }

   public List<Boolean> getSettings() {
      return Collections.unmodifiableList(this.settings);
   }

   public void setSettings(List<Boolean> items) {
      int value = items.size();
      this.settings.setMinNumberOfBits(value);

      for(int index = 0; index < value; ++index) {
         this.settings.set(index, (Boolean)items.get(index));
      }

   }
}
