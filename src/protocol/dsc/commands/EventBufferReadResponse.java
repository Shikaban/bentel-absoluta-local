package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscDateTime;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscStruct;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class EventBufferReadResponse extends DscCommandWithResponse.Response<EventBufferRead> implements DscArray.ElementProvider<EventBufferReadResponse.Event> {
   public static final int ABSOLUTA_EVENT_FLAGS = 9;
   private final DscArray<EventBufferReadResponse.Event> events = new DscArray<EventBufferReadResponse.Event>(this);

   public EventBufferReadResponse() {
      super(new EventBufferRead());
   }

   protected List<DscSerializable> getResponseFields() {
      return ImmutableList.of(this.events);
   }

   public int getCommandNumber() {
      return 16641;
   }

   public int getBufferID() {
      return ((EventBufferRead)this.requestInstance).getBufferID();
   }

   public int getEventNumber() {
      return ((EventBufferRead)this.requestInstance).getEventNumber();
   }

   public int getNumberOfEvents() {
      return ((EventBufferRead)this.requestInstance).getNumberOfEvents();
   }

   public List<EventBufferReadResponse.Event> getEvents() {
      return Collections.unmodifiableList(this.events);
   }

   public int numberOfElements() {
      return this.getNumberOfEvents();
   }

   public EventBufferReadResponse.Event newElement() {
      return new EventBufferReadResponse.Event();
   }

   public static class Event extends DscStruct {
      private final DscDateTime dateTimeStamp = new DscDateTime();
      private final DscNumber flags = DscNumber.newUnsignedNum(1);
      private final DscNumber eventId = DscNumber.newUnsignedNum(2);
      private final DscNumber whereWhy = DscNumber.newUnsignedNum(1);
      private final DscNumber who = DscNumber.newUnsignedNum(1);
      private final DscNumber partitionMaskUnusedBytes = DscNumber.newUnsignedNum(2);
      private final DscBitMask partitionMask = new DscBitMask(2, 1, false);

      protected List<DscSerializable> getFields() {
         return ImmutableList.of(this.dateTimeStamp, this.flags, this.eventId, this.whereWhy, this.who, this.partitionMaskUnusedBytes, this.partitionMask);
      }

      public Calendar getDateTimeStamp() {
         return this.dateTimeStamp.get();
      }

      public int getFlags() {
         return this.flags.toInt();
      }

      public int getEventClass() {
         return (this.eventId.toInt() & '\uf000') >>> 12;
      }

      public boolean isRestore() {
         return (this.eventId.toInt() & 2048) != 0;
      }

      public int getEventCode() {
         return this.eventId.toInt() & 2047;
      }

      public int getWhereWhy() {
         return this.whereWhy.toInt();
      }

      public int getWho() {
         return this.who.toInt();
      }

      public int getPartitionMaskUnusedBytes() {
         return this.partitionMaskUnusedBytes.toInt();
      }

      public List<Integer> getPartitions() {
         return this.partitionMask.getTrueIndexes();
      }
   }
}
