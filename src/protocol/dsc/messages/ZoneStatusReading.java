package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.base.DscBitMask;
import protocol.dsc.commands.ZoneStatus;

import java.util.List;

public class ZoneStatusReading extends RequestableCommandReading<Integer, List<Boolean>, ZoneStatus> {
   public ZoneStatusReading() {
      super(ZoneStatus.class);
   }

   protected ZoneStatus prepareRequest(ChannelHandlerContext var1, Integer var2) {
      ZoneStatus var3 = new ZoneStatus();
      var3.setZoneNumber(var2);
      var3.setNumberOfZones(1);
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, ZoneStatus var2, List<Message.Response> var3) {
      int var4 = var2.getZoneNumber();
      int var5 = var2.getNumberOfZones();
      List<DscBitMask> var6 = var2.getZoneStatuses();

      for(int var7 = 0; var7 < var5; ++var7) {
         List<Boolean> var8 = (List)var6.get(var7);
         var3.add(new NewValue(this, var4 + var7, var8));
      }

   }
}
