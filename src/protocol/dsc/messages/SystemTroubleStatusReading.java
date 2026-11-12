package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.CommandResponse;
import protocol.dsc.commands.SystemTroubleStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.javatuples.Pair;

public class SystemTroubleStatusReading extends RequestableCommandReading<Void, List<Pair<Integer, Integer>>, SystemTroubleStatus> {
   public SystemTroubleStatusReading() {
      super(SystemTroubleStatus.class);
   }

   protected SystemTroubleStatus prepareRequest(ChannelHandlerContext context, Void void2) throws Exception {
      SystemTroubleStatus systemTroubleStatus2 = new SystemTroubleStatus();
      systemTroubleStatus2.setRequestAll();
      return systemTroubleStatus2;
   }

   protected void parseResponse(ChannelHandlerContext context, SystemTroubleStatus systemTroubleStatus2, List<Message.Response> responses) {
      List<SystemTroubleStatus.Trouble> troubles = systemTroubleStatus2.getTroubles();
      List<Pair<Integer, Integer>> items = new ArrayList<Pair<Integer, Integer>>();
      for (SystemTroubleStatus.Trouble trouble : troubles) {
         int deviceModuleType = trouble.getDeviceModuleType();
         for (Integer troubleType : trouble.getTroubleTypes()) {
         items.add(Pair.with(deviceModuleType, troubleType));
         }
      }

      responses.add(new NewValue(this, items));
   }

   protected void parseCommandResponse(ChannelHandlerContext context, Void void2, CommandResponse commandResponse2, List<Message.Response> responses) {
      if (commandResponse2.getResponseCode() == 26) {
         responses.add(new NewValue(this, Collections.emptyList()));
      }

   }
}
