
package plugin.absoluta.connection;

import com.google.common.base.Joiner;

import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.NewValue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.javatuples.Pair;

class StatusReader implements MessageListener {
   private static final Joiner JOINER = Joiner.on(", ");
   private static final long POLL_INTERVAL;
   private static final long MESSAGE_NOTIF_TIMEOUT;
   private static final long TOTAL_NOTIF_TIMEOUT;
   private final MessageHandler messageHandler;
   private final ScheduledExecutorService executor;
   private List<Integer> partitions;
   private Pair<Integer, Integer> firstZoneAndZoneNum;
   private Long notificationsUponLoginBegin;
   private boolean partitionStatusesReceived;
   private ScheduledFuture<?> notificationFuture;
   private static final boolean VERBOSE_DEBUG = false;

   StatusReader(MessageHandler var1, ScheduledExecutorService var2) {
      this.messageHandler = (MessageHandler)Objects.requireNonNull(var1);
      this.executor = (ScheduledExecutorService)Objects.requireNonNull(var2);
   }

   void startNotificationsUponLoginWaiting() {
      this.executor.execute(new Runnable() {
         public void run() {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: notifications upon login waiting");
            }
            StatusReader.this.notificationsUponLoginBegin = System.currentTimeMillis();
            StatusReader.this.waitNotifications();
         }
      });
   }

   private void waitNotifications() {
      assert this.notificationsUponLoginBegin != null;

      if (this.notificationFuture != null) {
         this.notificationFuture.cancel(false);
      }

      if (System.currentTimeMillis() - this.notificationsUponLoginBegin < TOTAL_NOTIF_TIMEOUT) {
         this.notificationFuture = this.executor.schedule(new Runnable() {
            public void run() {
               if(VERBOSE_DEBUG) {
                  System.out.println("DEBUG: notifications upon login timeout");
               }
               StatusReader.this.stopNotificationsUponLoginWaiting();
            }
         }, MESSAGE_NOTIF_TIMEOUT, TimeUnit.MILLISECONDS);
      } else {
         System.out.println("WARN: notifications upon login timeout (reached maximum waiting time)");
         this.stopNotificationsUponLoginWaiting();
      }
   }

   private void stopNotificationsUponLoginWaiting() {
      this.notificationsUponLoginBegin = null;
      if (this.notificationFuture != null) {
         this.notificationFuture.cancel(false);
      }

      if (this.partitions == null) {
         this.messageHandler.sendHighPriorityReading(Message.PARTITION_ASSIGNMENT_CONFIGURATION, null);
      }

      if (this.firstZoneAndZoneNum == null) {
         this.messageHandler.sendHighPriorityReading(Message.PARTITION_ZONES, 0);
      }

      if (!this.partitionStatusesReceived && this.partitions != null) {
         this.messageHandler.sendHighPriorityReading(Message.PARTITION_STATUSES, this.partitions);
      }

      if (this.firstZoneAndZoneNum != null) {
         this.messageHandler.sendHighPriorityReading(Message.ZONE_STATUSES, this.firstZoneAndZoneNum);
      }

      this.messageHandler.sendHighPriorityReading(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS, null);
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: start reading and polling");
      }
      this.messageHandler.start();
      this.executor.scheduleAtFixedRate(new Runnable() {
         public void run() {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: status poll");
            }
            if (StatusReader.this.partitions != null) {
               StatusReader.this.messageHandler.sendMidPriorityReading(Message.PARTITION_STATUSES, StatusReader.this.partitions);
            }

            if (StatusReader.this.firstZoneAndZoneNum != null && (Integer)StatusReader.this.firstZoneAndZoneNum.getValue1() > 0) {
               StatusReader.this.messageHandler.sendMidPriorityReading(Message.ZONE_STATUSES, StatusReader.this.firstZoneAndZoneNum);
            }

         }
      }, POLL_INTERVAL, POLL_INTERVAL, TimeUnit.MILLISECONDS);
   }

   public void newValue(NewValue var1) {
      if (this.notificationsUponLoginBegin != null) {
         this.waitNotifications();
      }

      if (var1.isFor(Message.PARTITION_ASSIGNMENT_CONFIGURATION)) {
         this.partitions = (List)var1.getValue(Message.PARTITION_ASSIGNMENT_CONFIGURATION);
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: user partitions: " + JOINER.join(this.partitions));
         }
         this.messageHandler.sendMidPriorityReading(Message.ABSOLUTA_SYSTEM_LABEL, null);
         Iterator var2 = this.partitions.iterator();

         Integer var3;
         while(var2.hasNext()) {
            var3 = (Integer)var2.next();
            this.messageHandler.sendMidPriorityReading(Message.ABSOLUTA_PARTITION_LABEL, var3);
         }

         var2 = CustomizedArmingModes.ARMING_MODE_LABELS.keySet().iterator();

         while(var2.hasNext()) {
            var3 = (Integer)var2.next();
            this.messageHandler.sendMidPriorityReading(Message.ABSOLUTA_ARMING_MODE_LABEL, var3);
         }
      } else {
         Integer var4;
         List var5;
         Iterator var6;
         if (var1.isFor(Message.PARTITION_ZONES) && var1.getParam(Message.PARTITION_ZONES) == null) {
            var5 = (List)var1.getValue(Message.PARTITION_ZONES);
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: user zones: " + JOINER.join(var5));
            }
            if (!var5.isEmpty()) {
               int var7 = (Integer)Collections.min(var5);
               int var8 = (Integer)Collections.max(var5);
               this.firstZoneAndZoneNum = Pair.with(var7, var8 - var7 + 1);
               this.messageHandler.sendMidPriorityReading(Message.ZONE_STATUSES, this.firstZoneAndZoneNum);
            } else {
               this.firstZoneAndZoneNum = Pair.with(0, 0);
            }

            var6 = var5.iterator();

            while(var6.hasNext()) {
               var4 = (Integer)var6.next();
               this.messageHandler.sendMidPriorityReading(Message.ABSOLUTA_ZONE_LABEL, var4);
            }
         } else if (var1.isFor(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)) {
            this.messageHandler.sendMidPriorityReading(Message.ABSOLUTA_COMMAND_OUTPUT_ACTIVATION, null);
            var5 = (List)((Pair)var1.getValue(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)).getValue0();
            var6 = var5.iterator();

            while(var6.hasNext()) {
               var4 = (Integer)var6.next();
               this.messageHandler.sendMidPriorityReading(Message.ABSOLUTA_OUTPUT_LABEL, var4);
            }
         } else if (var1.isFor(Message.PARTITION_STATUSES)) {
            this.partitionStatusesReceived = true;
            if (this.notificationsUponLoginBegin != null) {
               if(VERBOSE_DEBUG) {
                  System.out.println("DEBUG: partition statuses received");
               }
               this.partitionStatusesReceived = true;
               this.stopNotificationsUponLoginWaiting();
            }
         } else if (var1.isFor(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)) {
            System.out.println("INFO: >>> >>> >>> " + var1.getValue(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS));
         }
      }
   }

   public void error(DscError var1) {
   }

   static {
      POLL_INTERVAL = TimeUnit.SECONDS.toMillis(5L);
      MESSAGE_NOTIF_TIMEOUT = TimeUnit.SECONDS.toMillis(5L);
      TOTAL_NOTIF_TIMEOUT = TimeUnit.SECONDS.toMillis(40L);
   }
}
