package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.EventBufferRead;
import protocol.dsc.commands.EventBufferReadResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.javatuples.Pair;
import org.javatuples.Septet;

public class EventBufferReading extends Reading<
      Pair<Integer, Integer>,
      List<Septet<Calendar, Integer, Boolean, Integer, Integer, Integer, List<Integer>>>,
      EventBufferReadResponse> {

   public EventBufferReading() {
      super(EventBufferReadResponse.class);
   }

   @Override
   protected void parseResponse(ChannelHandlerContext ctx, EventBufferReadResponse response, List<Message.Response> responses) {
      if (response.getBufferID() != 3) {
            System.out.println("WARN: unmanaged buffer ID: " + response.getBufferID());
            return;
      }

      List<EventBufferReadResponse.Event> events = response.getEvents();
      if (events.size() != response.getNumberOfEvents()) {
            System.out.println("WARN: event count mismatch");
            return;
      }

      Pair<Integer, Integer> eventInfo = Pair.with(response.getEventNumber(), response.getNumberOfEvents());
      List<Septet<Calendar, Integer, Boolean, Integer, Integer, Integer, List<Integer>>> eventDetails = new ArrayList<>(events.size());

      for (EventBufferReadResponse.Event event : events) {
            if (event.getFlags() != 9) {
               System.out.println("WARN: unexpected flags: " + event.getFlags());
               return;
            }

            int unusedBytes = event.getPartitionMaskUnusedBytes();
            List<Integer> partitions = null;
            if (unusedBytes == 0) {
               partitions = event.getPartitions();
            } else if (unusedBytes != 65535) {
               System.out.println("WARN: unexpected partition mask unused bytes: " + unusedBytes);
            }

            eventDetails.add(Septet.with(
                  event.getDateTimeStamp(),
                  event.getEventClass(),
                  event.isRestore(),
                  event.getEventCode(),
                  event.getWhereWhy(),
                  event.getWho(),
                  partitions
            ));
      }

      responses.add(new NewValue(this, eventInfo, eventDetails));
   }

   @Override
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext ctx, Pair<Integer, Integer> param) throws Exception {
      EventBufferRead command = new EventBufferRead();
      command.setBufferID(3);
      command.setEventNumber(param.getValue0());
      command.setNumberOfEvents(param.getValue1());
      return command;
   }
}
