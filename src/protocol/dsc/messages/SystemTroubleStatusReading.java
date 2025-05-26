package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.CommandResponse;
import protocol.dsc.commands.SystemTroubleStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.javatuples.Pair;

public class SystemTroubleStatusReading extends RequestableCommandReading<Void, List<Pair<Integer, Integer>>, SystemTroubleStatus> {
   public SystemTroubleStatusReading() {
      super(SystemTroubleStatus.class);
   }

   protected SystemTroubleStatus prepareRequest(ChannelHandlerContext var1, Void var2) throws Exception {
      SystemTroubleStatus var3 = new SystemTroubleStatus();
      var3.setRequestAll();
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, SystemTroubleStatus var2, List<Message.Response> var3) {
      List<SystemTroubleStatus.Trouble> var4 = var2.getTroubles();
      List<Pair<Integer, Integer>> var5 = new ArrayList();
      Iterator var6 = var4.iterator();

      while(var6.hasNext()) {
         SystemTroubleStatus.Trouble var7 = (SystemTroubleStatus.Trouble)var6.next();
         int var8 = var7.getDeviceModuleType();
         Iterator var9 = var7.getTroubleTypes().iterator();

         while(var9.hasNext()) {
            Integer var10 = (Integer)var9.next();
            var5.add(Pair.with(var8, var10));
         }
      }

      var3.add(new NewValue(this, var5));
   }

   protected void parseCommandResponse(ChannelHandlerContext var1, Void var2, CommandResponse var3, List<Message.Response> var4) {
      if (var3.getResponseCode() == 26) {
         var4.add(new NewValue(this, Collections.emptyList()));
      }

   }
}
