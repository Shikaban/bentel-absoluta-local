package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.CommandResponse;
import protocol.dsc.commands.ZoneAlarmStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.javatuples.Triplet;

public class PartitionZoneAlarmStatusReading extends RequestableCommandReading<Integer, List<Triplet<Integer, Integer, Boolean>>, ZoneAlarmStatus> {
   public PartitionZoneAlarmStatusReading() {
      super(ZoneAlarmStatus.class);
   }

   @Override
   protected ZoneAlarmStatus prepareRequest(ChannelHandlerContext ctx, Integer partitionNumber) throws Exception {
      ZoneAlarmStatus request = new ZoneAlarmStatus();
      request.setPartitionNumber(partitionNumber);
      request.setRequestsAllZones();
      return request;
   }

   @Override
   protected void parseResponse(ChannelHandlerContext ctx, ZoneAlarmStatus response, List<Message.Response> responses) {
      List<ZoneAlarmStatus.ZoneAlarm> alarms = response.getAlarms();
      List<Triplet<Integer, Integer, Boolean>> alarmTriplets = new ArrayList<>(alarms.size());

      for (ZoneAlarmStatus.ZoneAlarm alarm : alarms) {
         alarmTriplets.add(Triplet.with(alarm.getZoneNumber(), alarm.getTypeOfAlarm(), alarm.getAlarmState()));
      }

      responses.add(new NewValue(this, response.getPartitionNumber(), alarmTriplets));
   }

   @Override
   protected void parseCommandResponse(ChannelHandlerContext ctx, Integer partitionNumber, CommandResponse response, List<Message.Response> responses) {
      if (response.getResponseCode() == 27) {
         responses.add(new NewValue(this, partitionNumber, Collections.emptyList()));
      }
   }
}
