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
}
