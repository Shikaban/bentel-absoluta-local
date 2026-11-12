package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public class EventReportingConfigurationRead extends DscCommandWithResponse {
   public static final int NOTIFICATION_SETTINGS_IN_REAL_TIME_BIT = 0;
   public static final int LOG_EVENTS_IN_LIFESTYLE_BUFFER_BIT = 1;
   private final DscBitMask reportingType = new DscBitMask(1, 0);
   private final DscNumber eventType = DscNumber.newUnsignedNum(1);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.reportingType, this.eventType);
   }

   public int getCommandNumber() {
      return 1889;
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
}
