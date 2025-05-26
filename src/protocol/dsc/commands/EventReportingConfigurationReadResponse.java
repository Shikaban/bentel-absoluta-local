package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscSerializable;

import java.util.Collections;
import java.util.List;

public class EventReportingConfigurationReadResponse extends DscCommandWithResponse.Response<EventReportingConfigurationRead> {
   private final DscBitMask settings = new DscBitMask(true, 0);

   public EventReportingConfigurationReadResponse() {
      super(new EventReportingConfigurationRead());
   }

   protected List<DscSerializable> getResponseFields() {
      return ImmutableList.of(this.settings);
   }

   public int getCommandNumber() {
      return 18273;
   }

   public boolean getReportingType(int var1) {
      return ((EventReportingConfigurationRead)this.requestInstance).getReportingType(var1);
   }

   public int getEventType() {
      return ((EventReportingConfigurationRead)this.requestInstance).getEventType();
   }

   public List<Boolean> getSettings() {
      return Collections.unmodifiableList(this.settings);
   }
}
