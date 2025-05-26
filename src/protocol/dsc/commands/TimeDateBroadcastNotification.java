package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscDateTime;
import protocol.dsc.base.DscSerializable;

import java.util.Calendar;
import java.util.List;

public class TimeDateBroadcastNotification extends DscRequestableCommand {
   private final DscDateTime dateTime = new DscDateTime().set(Calendar.getInstance());

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of();
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.dateTime);
   }

   public int getCommandNumber() {
      return 544;
   }

   public String getString() {
      return this.dateTime.toString();
   }
}
