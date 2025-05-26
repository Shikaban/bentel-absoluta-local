package protocol.dsc.transport.command_handlers;

import com.google.common.collect.ImmutableSet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.AbstractPartitionReqCommand;
import protocol.dsc.commands.AccessLevelLeadInOut;
import protocol.dsc.commands.ArmingDisarmingNotification;
import protocol.dsc.commands.ArmingPreAlertNotification;
import protocol.dsc.commands.EntryDelayNotification;
import protocol.dsc.commands.ExitDelayNotification;
import protocol.dsc.commands.LifeStyleZoneStatus;
import protocol.dsc.commands.MiscellaneousPreAlertNotification;
import protocol.dsc.commands.PartitionAlarmMemoryNotification;
import protocol.dsc.commands.PartitionQuickExitNotification;
import protocol.dsc.commands.PartitionReadyStatusNotification;
import protocol.dsc.commands.TimeDateBroadcastNotification;

import java.util.Set;
import org.javatuples.Quartet;

@Sharable
public class MiscNotificationHandler extends ChannelInboundHandlerAdapter {
   private static final Set<Class<? extends AbstractPartitionReqCommand>> PARTITION_STATUS_NOTIFICATIONS = ImmutableSet.of(ArmingDisarmingNotification.class, ArmingPreAlertNotification.class, EntryDelayNotification.class, ExitDelayNotification.class, MiscellaneousPreAlertNotification.class, PartitionAlarmMemoryNotification.class, new Class[]{PartitionQuickExitNotification.class, PartitionReadyStatusNotification.class});
   private static final boolean VERBOSE_DEBUG = false;

   public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
      if (PARTITION_STATUS_NOTIFICATIONS.contains(var2.getClass())) {
         AbstractPartitionReqCommand var3 = (AbstractPartitionReqCommand)var2;
         Integer var4 = var3.getPartitionNumber();
         if (var4 != null) {
            var1.fireChannelRead(new NewValue(Message.PARTITION_STATUS_CHANGED, var4));
            if (var3 instanceof PartitionAlarmMemoryNotification) {
               var1.fireChannelRead(new NewValue(Message.PARTITION_ALARM_MEMORY_CHANGED, var4));
            }
         }
      } else if (var2 instanceof LifeStyleZoneStatus) {
         LifeStyleZoneStatus var5 = (LifeStyleZoneStatus)var2;
         int var7 = var5.getZoneNumber();
         var1.fireChannelRead(new NewValue(Message.ZONE_STATUS_CHANGED, var7));
      } else if (var2 instanceof AccessLevelLeadInOut) {
         AccessLevelLeadInOut var6 = (AccessLevelLeadInOut)var2;
         if (var6.getPartitionNumber() == null) {
            var1.fireChannelRead(new NewValue(Message.ACCESS_LEVEL_LEAD_IN_OUT, Quartet.with(var6.getType(), var6.getUser(), var6.getAccess(), var6.getMode())));
         } else {
            System.out.println("WARN: unexpected partition for AccessLevelLeadInOut: " + var6.getPartitionNumber());
         }
      } else if (var2 instanceof TimeDateBroadcastNotification) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: time and date received: " + ((TimeDateBroadcastNotification) var1).toString());
         }
      } else {
         super.channelRead(var1, var2);
      }
   }
}
