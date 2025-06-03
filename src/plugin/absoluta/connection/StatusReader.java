package plugin.absoluta.connection;

import com.google.common.base.Joiner;
import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.NewValue;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.javatuples.Pair;

class StatusReader implements MessageListener {
   private static final Joiner JOINER = Joiner.on(", ");
   private static final long POLL_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5);
   private static final long MESSAGE_NOTIFICATION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5);
   private static final long TOTAL_NOTIFICATION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(40);

   private final MessageHandler messageHandler;
   private final ScheduledExecutorService executor;
   private List<Integer> userPartitions;
   private Pair<Integer, Integer> firstZoneAndZoneCount;
   private Long notificationWaitStartTime;
   private boolean partitionStatusesReceived;
   private ScheduledFuture<?> notificationTimeoutFuture;
   private static final boolean VERBOSE_DEBUG = false;

   StatusReader(MessageHandler messageHandler, ScheduledExecutorService executor) {
      this.messageHandler = Objects.requireNonNull(messageHandler);
      this.executor = Objects.requireNonNull(executor);
   }

   void startWaitingForNotificationsAfterLogin() {
      executor.execute(() -> {
         if (VERBOSE_DEBUG) {
            System.out.println("DEBUG: Waiting for notifications after login");
         }
         notificationWaitStartTime = System.currentTimeMillis();
         waitForNotifications();
      });
   }

   private void waitForNotifications() {
      assert notificationWaitStartTime != null;

      if (notificationTimeoutFuture != null) {
         notificationTimeoutFuture.cancel(false);
      }

      long elapsed = System.currentTimeMillis() - notificationWaitStartTime;
      if (elapsed < TOTAL_NOTIFICATION_TIMEOUT_MS) {
         notificationTimeoutFuture = executor.schedule(() -> {
            if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: Notification wait timeout");
            }
            stopWaitingForNotificationsAfterLogin();
         }, MESSAGE_NOTIFICATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
      } else {
         System.out.println("WARN: Notification wait timeout (max wait time reached)");
         stopWaitingForNotificationsAfterLogin();
      }
   }

   private void stopWaitingForNotificationsAfterLogin() {
      notificationWaitStartTime = null;
      if (notificationTimeoutFuture != null) {
         notificationTimeoutFuture.cancel(false);
      }

      if (userPartitions == null) {
         messageHandler.sendHighPriorityReading(Message.PARTITION_ASSIGNMENT_CONFIGURATION, null);
      }

      if (firstZoneAndZoneCount == null) {
         messageHandler.sendHighPriorityReading(Message.PARTITION_ZONES, 0);
      }

      if (!partitionStatusesReceived && userPartitions != null) {
         messageHandler.sendHighPriorityReading(Message.PARTITION_STATUSES, userPartitions);
      }

      if (firstZoneAndZoneCount != null) {
         messageHandler.sendHighPriorityReading(Message.ZONE_STATUSES, firstZoneAndZoneCount);
      }

      messageHandler.sendHighPriorityReading(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS, null);

      if (VERBOSE_DEBUG) {
         System.out.println("DEBUG: Start reading and polling");
      }
      messageHandler.start();

      executor.scheduleAtFixedRate(() -> {
         if (VERBOSE_DEBUG) {
            System.out.println("DEBUG: Status poll");
         }
         if (userPartitions != null) {
            messageHandler.sendMidPriorityReading(Message.PARTITION_STATUSES, userPartitions);
         }
         if (firstZoneAndZoneCount != null && firstZoneAndZoneCount.getValue1() > 0) {
            messageHandler.sendMidPriorityReading(Message.ZONE_STATUSES, firstZoneAndZoneCount);
         }
      }, POLL_INTERVAL_MS, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
   }

   @Override
   public void newValue(NewValue newValue) {
      if (notificationWaitStartTime != null) {
         waitForNotifications();
      }

      if (newValue.isFor(Message.PARTITION_ASSIGNMENT_CONFIGURATION)) {
         userPartitions = (List<Integer>) newValue.getValue(Message.PARTITION_ASSIGNMENT_CONFIGURATION);
         if (VERBOSE_DEBUG) {
            System.out.println("DEBUG: User partitions: " + JOINER.join(userPartitions));
         }
         messageHandler.sendMidPriorityReading(Message.ABSOLUTA_SYSTEM_LABEL, null);
         for (Integer partition : userPartitions) {
            messageHandler.sendMidPriorityReading(Message.ABSOLUTA_PARTITION_LABEL, partition);
         }
         for (Integer armingMode : CustomizedArmingModes.ARMING_MODE_LABELS.keySet()) {
            messageHandler.sendMidPriorityReading(Message.ABSOLUTA_ARMING_MODE_LABEL, armingMode);
         }
      } else if (newValue.isFor(Message.PARTITION_ZONES) && newValue.getParam(Message.PARTITION_ZONES) == null) {
         List<Integer> userZones = (List<Integer>) newValue.getValue(Message.PARTITION_ZONES);
         if (VERBOSE_DEBUG) {
            System.out.println("DEBUG: User zones: " + JOINER.join(userZones));
         }
         if (!userZones.isEmpty()) {
            int minZone = Collections.min(userZones);
            int maxZone = Collections.max(userZones);
            firstZoneAndZoneCount = Pair.with(minZone, maxZone - minZone + 1);
            messageHandler.sendMidPriorityReading(Message.ZONE_STATUSES, firstZoneAndZoneCount);
         } else {
            firstZoneAndZoneCount = Pair.with(0, 0);
         }
         for (Integer zone : userZones) {
            messageHandler.sendMidPriorityReading(Message.ABSOLUTA_ZONE_LABEL, zone);
         }
      } else if (newValue.isFor(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)) {
         messageHandler.sendMidPriorityReading(Message.ABSOLUTA_COMMAND_OUTPUT_ACTIVATION, null);
         List<Integer> outputs = (List<Integer>) ((Pair<?, ?>) newValue.getValue(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)).getValue0();
         for (Integer output : outputs) {
            messageHandler.sendMidPriorityReading(Message.ABSOLUTA_OUTPUT_LABEL, output);
         }
      } else if (newValue.isFor(Message.PARTITION_STATUSES)) {
         partitionStatusesReceived = true;
         if (notificationWaitStartTime != null) {
            if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: Partition statuses received");
            }
            partitionStatusesReceived = true;
            stopWaitingForNotificationsAfterLogin();
         }
      } else if (newValue.isFor(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)) {
         System.out.println("INFO: >>> >>> >>> " + newValue.getValue(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS));
      }
   }

   @Override
   public void error(DscError error) {
      // Handle error if needed
   }
}
