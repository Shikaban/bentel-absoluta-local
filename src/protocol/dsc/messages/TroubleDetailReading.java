package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.CommandResponse;
import protocol.dsc.commands.TroubleDetail;

import java.util.Collections;
import java.util.List;
import org.javatuples.Pair;

public class TroubleDetailReading extends RequestableCommandReading<Pair<Integer, Integer>, List<Integer>, TroubleDetail> {
   public TroubleDetailReading() {
      super(TroubleDetail.class);
   }

   protected TroubleDetail prepareRequest(ChannelHandlerContext context, Pair<Integer, Integer> pair2) throws Exception {
      TroubleDetail troubleDetail2 = new TroubleDetail();
      troubleDetail2.setDeviceModuleType((Integer)pair2.getValue0());
      troubleDetail2.setTroubleType((Integer)pair2.getValue1());
      return troubleDetail2;
   }

   protected void parseResponse(ChannelHandlerContext context, TroubleDetail troubleDetail2, List<Message.Response> responses) {
      Pair<Integer, Integer> deviceModuleType = Pair.with(troubleDetail2.getDeviceModuleType(), troubleDetail2.getTroubleType());
      responses.add(new NewValue(this, deviceModuleType, troubleDetail2.getDeviceModuleNumbers()));
   }

   protected void parseCommandResponse(ChannelHandlerContext context, Pair<Integer, Integer> pair2, CommandResponse commandResponse2, List<Message.Response> responses) {
      if (commandResponse2.getResponseCode() == 26) {
         responses.add(new NewValue(this, pair2, Collections.emptyList()));
      }

   }
}
