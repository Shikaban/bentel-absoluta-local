package protocol.dsc.transport.command_handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.TroubleDetailNotification;

import java.util.ArrayList;
import java.util.List;
import org.javatuples.Quartet;

@Sharable
public class TroubleDetailNotificationHandler extends ChannelInboundHandlerAdapter {
   private static final Integer ZONE_DEVICE = 1;

   public void channelRead(ChannelHandlerContext context, Object object2) throws Exception {
      if (object2 instanceof TroubleDetailNotification) {
         TroubleDetailNotification troubleDetailNotification2 = (TroubleDetailNotification)object2;
         List<TroubleDetailNotification.Trouble> troubles = troubleDetailNotification2.getTroubles();
         List<Quartet<Integer, Integer, Integer, Integer>> items = new ArrayList<Quartet<Integer, Integer, Integer, Integer>>(troubles.size());
         for (TroubleDetailNotification.Trouble trouble : troubles) {
            items.add(Quartet.with(trouble.getDeviceModuleType(), trouble.getTroubleType(), trouble.getDeviceModuleNumber(), trouble.getStatus()));
            if (ZONE_DEVICE.equals(trouble.getDeviceModuleType())) {
               context.fireChannelRead(new NewValue(Message.ZONE_STATUS_CHANGED, trouble.getDeviceModuleNumber()));
            }
         }

         context.fireChannelRead(new NewValue(Message.TROUBLE_DETAIL_NOTIFICATION, items));
      } else {
         super.channelRead(context, object2);
      }

   }
}
