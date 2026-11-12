package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public class EventBufferRead extends DscCommandWithResponse {
   public static final int PANEL_EVENT_BUFFER_TYPE_B = 3;
   private final DscNumber bufferID = DscNumber.newUnsignedNum(1);
   private final DscNumber eventNumber = DscNumber.newUnsignedNum(2);
   private final DscNumber numberOfEvents = DscNumber.newUnsignedNum(2);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.bufferID, this.eventNumber, this.numberOfEvents);
   }

   public int getCommandNumber() {
      return 257;
   }

   public int getBufferID() {
      return this.bufferID.toInt();
   }

   public void setBufferID(int value) {
      this.bufferID.set((long)value);
   }

   public int getEventNumber() {
      return this.eventNumber.toInt();
   }

   public void setEventNumber(int value) {
      this.eventNumber.set((long)value);
   }

   public int getNumberOfEvents() {
      return this.numberOfEvents.toInt();
   }

   public void setNumberOfEvents(int value) {
      this.numberOfEvents.set((long)value);
   }
}
