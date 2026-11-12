package absoluta.connection;

import com.google.common.base.Joiner;

import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.NewValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.javatuples.Pair;

class StatusReader implements MessageListener {
   private static final Logger logger = Logger.getLogger(StatusReader.class.getName());
   private static final Joiner JOINER = Joiner.on(", ");
   private static final long POLL_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5);
   private static final long MESSAGE_NOTIFICATION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5);
   private static final long TOTAL_NOTIFICATION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(40);

   private final MessageHandler messageHandler;
   private final ScheduledExecutorService executor;
   private List<Integer> userPartitions;
   private List<Integer> userZones;
   private List<Pair<Integer, Integer>> zoneRuns;
   private boolean singleZoneMode;
   private Long notificationWaitStartTime;
   private boolean partitionStatusesReceived;
   private ScheduledFuture<?> notificationTimeoutFuture;

   StatusReader(MessageHandler messageHandler, ScheduledExecutorService executor) {
      this.messageHandler = Objects.requireNonNull(messageHandler);
      this.executor = Objects.requireNonNull(executor);
   }

   void startWaitingForNotificationsAfterLogin() {
      executor.execute(() -> {
         logger.fine("Waiting for notifications after login");
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
            logger.fine("Notification wait timeout");
            stopWaitingForNotificationsAfterLogin();
         }, MESSAGE_NOTIFICATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
      } else {
         logger.warning("Notification wait timeout (max wait time reached)");
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

      if (zoneRuns == null) {
         messageHandler.sendHighPriorityReading(Message.PARTITION_ZONES, 0);
      }

      if (!partitionStatusesReceived && userPartitions != null) {
         messageHandler.sendHighPriorityReading(Message.PARTITION_STATUSES, userPartitions);
      }

      sendZoneStatusRuns(true);

      messageHandler.sendHighPriorityReading(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS, null);

      logger.fine("Start reading and polling");
      messageHandler.start();

      executor.scheduleAtFixedRate(() -> {
         logger.fine("Status poll");
         if (userPartitions != null) {
            messageHandler.sendMidPriorityReading(Message.PARTITION_STATUSES, userPartitions);
         }
         sendZoneStatusRuns(false);
      }, POLL_INTERVAL_MS, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
   }

   @Override
   @SuppressWarnings("unchecked")
   public void newValue(NewValue newValue) {
      if (notificationWaitStartTime != null) {
         waitForNotifications();
      }

      if (newValue.isFor(Message.PARTITION_ASSIGNMENT_CONFIGURATION)) {
         userPartitions = (List<Integer>) newValue.getValue(Message.PARTITION_ASSIGNMENT_CONFIGURATION);
         logger.fine("User partitions: " + JOINER.join(userPartitions));
         messageHandler.sendMidPriorityReading(Message.ABSOLUTA_SYSTEM_LABEL, null);
         for (Integer partition : userPartitions) {
            messageHandler.sendMidPriorityReading(Message.ABSOLUTA_PARTITION_LABEL, partition);
         }
         for (Integer armingMode : CustomizedArmingModes.ARMING_MODE_LABELS.keySet()) {
            messageHandler.sendMidPriorityReading(Message.ABSOLUTA_ARMING_MODE_LABEL, armingMode);
         }
      } else if (newValue.isFor(Message.PARTITION_ZONES) && newValue.getParam(Message.PARTITION_ZONES) == null) {
         userZones = (List<Integer>) newValue.getValue(Message.PARTITION_ZONES);
         logger.fine("User zones: " + JOINER.join(userZones));
         zoneRuns = toContiguousRuns(userZones);
         logger.fine("Zone status request runs: " + JOINER.join(zoneRuns));
         sendZoneStatusRuns(false);
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
            logger.fine("Partition statuses received");
            partitionStatusesReceived = true;
            stopWaitingForNotificationsAfterLogin();
         }
      } else if (newValue.isFor(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)) {
         logger.finer(">>> >>> >>> " + newValue.getValue(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS));
      } else if (newValue.isFor(Message.SYSTEM_TROUBLE_STATUS)) {
         List<Pair<Integer, Integer>> troubles =
            (List<Pair<Integer, Integer>>) newValue.getValue(Message.SYSTEM_TROUBLE_STATUS);
         for (Pair<Integer, Integer> pair : troubles) {
            messageHandler.sendMidPriorityReading(Message.TROUBLE_DETAIL, pair);
         }
      }
   }

   @Override
   public void error(DscError error) {
      // Handle error if needed
   }

   /**
    * Sends a {@code ZONE_STATUSES} reading for every current zone run. Each request carries a
    * fallback so a timeout is never fatal: if a multi-zone run keeps timing out (some panels do
    * not answer multi-zone {@code ZONE_STATUSES} at all), {@link #handleZoneRunAbandoned} degrades
    * the session to single-zone reads instead of dropping the connection.
    */
   private void sendZoneStatusRuns(boolean highPriority) {
      if (zoneRuns == null) {
         return;
      }
      for (Pair<Integer, Integer> run : zoneRuns) {
         Runnable onAbandon = () -> handleZoneRunAbandoned(run);
         if (highPriority) {
            messageHandler.sendHighPriorityReading(Message.ZONE_STATUSES, run, onAbandon);
         } else {
            messageHandler.sendMidPriorityReading(Message.ZONE_STATUSES, run, onAbandon);
         }
      }
   }

   /**
    * Invoked when a {@code ZONE_STATUSES} run has exhausted its retries. For a multi-zone run this
    * means the panel does not support multi-zone reads, so the session switches to single-zone
    * mode (future polls read one zone at a time) and the zones of the failed run are re-queued as
    * single reads. A single-zone run that the panel still ignores is simply skipped.
    */
   private void handleZoneRunAbandoned(Pair<Integer, Integer> failedRun) {
      int start = failedRun.getValue0();
      int count = failedRun.getValue1();
      if (count <= 1) {
         return;
      }
      if (!singleZoneMode) {
         singleZoneMode = true;
         zoneRuns = toSingleZoneRuns(userZones);
         logger.warning("Panel did not answer ZONE_STATUSES" + failedRun
            + "; falling back to single-zone reads");
      }
      for (int zone = start; zone < start + count; zone++) {
         if (userZones.contains(zone)) {
            Pair<Integer, Integer> singleRun = Pair.with(zone, 1);
            messageHandler.sendMidPriorityReading(
               Message.ZONE_STATUSES, singleRun, () -> handleZoneRunAbandoned(singleRun));
         }
      }
   }

   /** Builds one single-zone {@code (zone, 1)} run per enabled zone, for panels that reject multi-zone reads. */
   private static List<Pair<Integer, Integer>> toSingleZoneRuns(List<Integer> zones) {
      List<Pair<Integer, Integer>> runs = new ArrayList<>();
      List<Integer> sorted = new ArrayList<>(zones);
      Collections.sort(sorted);
      int prev = Integer.MIN_VALUE;
      for (int zone : sorted) {
         if (zone != prev) {
            runs.add(Pair.with(zone, 1));
            prev = zone;
         }
      }
      return runs;
   }

   /**
    * Splits a sparse list of enabled/assigned zones into the minimal set of contiguous
    * (firstZone, count) runs. The DSC {@code ZoneStatus} command can only request a
    * contiguous block, so collapsing the whole list to a single min..max range would also
    * poll disabled zones. Splitting into runs requests statuses for enabled zones only.
    */
   private static List<Pair<Integer, Integer>> toContiguousRuns(List<Integer> zones) {
      List<Pair<Integer, Integer>> runs = new ArrayList<>();
      if (zones.isEmpty()) {
         return runs;
      }

      List<Integer> sorted = new ArrayList<>(zones);
      Collections.sort(sorted);

      int runStart = sorted.get(0);
      int prev = runStart;
      for (int i = 1; i < sorted.size(); i++) {
         int zone = sorted.get(i);
         if (zone == prev) {
            continue;
         }
         if (zone == prev + 1) {
            prev = zone;
         } else {
            runs.add(Pair.with(runStart, prev - runStart + 1));
            runStart = zone;
            prev = zone;
         }
      }
      runs.add(Pair.with(runStart, prev - runStart + 1));
      return runs;
   }
}
