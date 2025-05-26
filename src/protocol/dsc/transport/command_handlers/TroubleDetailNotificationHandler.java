package protocol.dsc.transport.command_handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.TroubleDetailNotification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.javatuples.Quartet;

@Sharable
public class TroubleDetailNotificationHandler extends ChannelInboundHandlerAdapter {
   private static final Integer ZONE_DEVICE = 1;

   public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
      if (var2 instanceof TroubleDetailNotification) {
         TroubleDetailNotification var3 = (TroubleDetailNotification)var2;
         List<TroubleDetailNotification.Trouble> var4 = var3.getTroubles();
         List<Quartet<Integer, Integer, Integer, Integer>> var5 = new ArrayList(var4.size());
         Iterator var6 = var4.iterator();

         while(var6.hasNext()) {
            TroubleDetailNotification.Trouble var7 = (TroubleDetailNotification.Trouble)var6.next();
            var5.add(Quartet.with(var7.getDeviceModuleType(), var7.getTroubleType(), var7.getDeviceModuleNumber(), var7.getStatus()));
            if (ZONE_DEVICE.equals(var7.getDeviceModuleType())) {
               var1.fireChannelRead(new NewValue(Message.ZONE_STATUS_CHANGED, var7.getDeviceModuleNumber()));
            }
         }

         var1.fireChannelRead(new NewValue(Message.TROUBLE_DETAIL_NOTIFICATION, var5));
      } else {
         super.channelRead(var1, var2);
      }

   }
}
