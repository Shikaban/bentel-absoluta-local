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

   protected TroubleDetail prepareRequest(ChannelHandlerContext var1, Pair<Integer, Integer> var2) throws Exception {
      TroubleDetail var3 = new TroubleDetail();
      var3.setDeviceModuleType((Integer)var2.getValue0());
      var3.setTroubleType((Integer)var2.getValue1());
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, TroubleDetail var2, List<Message.Response> var3) {
      Pair<Integer, Integer> var4 = new Pair(var2.getDeviceModuleType(), var2.getTroubleType());
      var3.add(new NewValue(this, var4, var2.getDeviceModuleNumbers()));
   }

   protected void parseCommandResponse(ChannelHandlerContext var1, Pair<Integer, Integer> var2, CommandResponse var3, List<Message.Response> var4) {
      if (var3.getResponseCode() == 26) {
         var4.add(new NewValue(this, var2, Collections.emptyList()));
      }

   }
}
