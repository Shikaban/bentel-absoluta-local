package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.CommandResponse;
import protocol.dsc.commands.ZoneAlarmStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.javatuples.Triplet;

public class PartitionZoneAlarmStatusReading extends RequestableCommandReading<Integer, List<Triplet<Integer, Integer, Boolean>>, ZoneAlarmStatus> {
   public PartitionZoneAlarmStatusReading() {
      super(ZoneAlarmStatus.class);
   }

   protected ZoneAlarmStatus prepareRequest(ChannelHandlerContext var1, Integer var2) throws Exception {
      ZoneAlarmStatus var3 = new ZoneAlarmStatus();
      var3.setPartitionNumber(var2);
      var3.setRequestsAllZones();
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, ZoneAlarmStatus var2, List<Message.Response> var3) {
      List<ZoneAlarmStatus.ZoneAlarm> var4 = var2.getAlarms();
      List<Triplet<Integer, Integer, Boolean>> var5 = new ArrayList(var4.size());
      Iterator var6 = var4.iterator();

      while(var6.hasNext()) {
         ZoneAlarmStatus.ZoneAlarm var7 = (ZoneAlarmStatus.ZoneAlarm)var6.next();
         var5.add(new Triplet(var7.getZoneNumber(), var7.getTypeOfAlarm(), var7.getAlarmState()));
      }

      var3.add(new NewValue(this, var2.getPartitionNumber(), var5));
   }

   protected void parseCommandResponse(ChannelHandlerContext var1, Integer var2, CommandResponse var3, List<Message.Response> var4) {
      if (var3.getResponseCode() == 27) {
         var4.add(new NewValue(this, var2, Collections.emptyList()));
      }

   }
}
