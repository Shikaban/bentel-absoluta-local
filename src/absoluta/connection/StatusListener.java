package absoluta.connection;

import com.google.common.collect.ImmutableList;

import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.NewValue;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.javatuples.Pair;
import org.javatuples.Quartet;

class StatusListener implements MessageListener {
   private static final Logger logger = Logger.getLogger(StatusListener.class.getName());

   // Partition status indices
   private static final int PARTITION_ARMED = 0;
   private static final int PARTITION_STAY = 1;
   // PARTITION_READY shares index 1: bit1 means "Ready to Arm" when disarmed, "Stay" when armed (ITv2 spec 6.7.4)
   private static final int PARTITION_READY = 1;
   private static final int PARTITION_AWAY = 2;
   private static final int PARTITION_NIGHT = 3;
   private static final int PARTITION_NODELAY = 4;
   private static final int PARTITION_ALARM = 8;
   private static final int PARTITION_TROUBLES = 9;
   private static final int PARTITION_ALARM_IN_MEMORY = 12;
   private static final int PARTITION_FIRE = 17;

   // Zone status indices
   private static final int ZONE_OPEN = 0;
   private static final int ZONE_TAMPER = 1;
   private static final int ZONE_FAULT = 2;
   private static final int ZONE_LOW_BATTERY = 3;
   private static final int ZONE_DELINQUENCY = 4;
   private static final int ZONE_ALARM = 5;
   private static final int ZONE_ALARM_IN_MEMORY = 6;
   private static final int ZONE_BYPASSED = 7;

   private final PanelStatus panelStatus;
   private int partitionStatusUpdateSkipCount = 0;

   StatusListener(PanelStatus panelStatus) {
      this.panelStatus = Objects.requireNonNull(panelStatus);
   }

   @Override
   @SuppressWarnings("unchecked")
   public void newValue(NewValue msg) {
      if (msg.isFor(Message.PARTITION_ASSIGNMENT_CONFIGURATION)) {
         List<Integer> partitions = (List<Integer>) msg.getValue(Message.PARTITION_ASSIGNMENT_CONFIGURATION);
         panelStatus.updatePartitions(ImmutableList.copyOf(partitions));
      } else if (msg.isFor(Message.PARTITION_ZONES)) {
         Integer partitionNumber = (Integer) msg.getParam(Message.PARTITION_ZONES);
         if (partitionNumber == null) {
               List<Integer> zones = (List<Integer>) msg.getValue(Message.PARTITION_ZONES);
               panelStatus.updateZones(ImmutableList.copyOf(zones));
         } else {
            logger.warning("Unexpected partition number for partition zones: " + partitionNumber);
         }
      } else if (msg.isFor(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)) {
         List<Integer> outputs = (List<Integer>) ((Pair<?, ?>) msg.getValue(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)).getValue0();
         panelStatus.updateOutputs(ImmutableList.copyOf(outputs));
      } else if (msg.isFor(Message.PARTITION_STATUSES)) {
         List<Integer> partitionIds = (List<Integer>) msg.getParam(Message.PARTITION_STATUSES);
         List<List<Boolean>> partitionStatuses = (List<List<Boolean>>) msg.getValue(Message.PARTITION_STATUSES);

            if (partitionIds.size() != partitionStatuses.size()) {
               logger.warning("Partition IDs and statuses size mismatch");
               return;
            }

            for (int i = 0; i < partitionIds.size(); ++i) {
               int partitionId = partitionIds.get(i);
               List<Boolean> statusMask = partitionStatuses.get(i);
               updatePartitionStatus(partitionId, statusMask);
            }
      } else if (msg.isFor(Message.ZONE_STATUSES)) {
         int zoneId = (Integer) ((Pair<?, ?>) msg.getParam(Message.ZONE_STATUSES)).getValue0();
         List<List<Boolean>> zoneStatuses = (List<List<Boolean>>) msg.getValue(Message.ZONE_STATUSES);

         for (List<Boolean> statusMask : zoneStatuses) {
               updateZoneStatus(zoneId, statusMask);
               zoneId++;
         }
      } else if (msg.isFor(Message.ABSOLUTA_COMMAND_OUTPUT_ACTIVATION)) {
         List<Integer> activeOutputs = (List<Integer>) msg.getValue(Message.ABSOLUTA_COMMAND_OUTPUT_ACTIVATION);
         for (Integer outputId : panelStatus.getOutputs()) {
               PanelStatus.OutputStatus outputStatus = activeOutputs.contains(outputId) ? PanelStatus.OutputStatus.CLOSED : PanelStatus.OutputStatus.OPEN;
               panelStatus.updateOutputStatus(outputId, outputStatus);
         }
      } else if (msg.isFor(Message.ABSOLUTA_SYSTEM_LABEL)) {
         String systemLabel = ((String) msg.getValue(Message.ABSOLUTA_SYSTEM_LABEL)).trim();
         panelStatus.updateSystemLabel(systemLabel);
      } else if (msg.isFor(Message.ABSOLUTA_PARTITION_LABEL)) {
         int partitionId = (Integer) msg.getParam(Message.ABSOLUTA_PARTITION_LABEL);
         String label = ((String) msg.getValue(Message.ABSOLUTA_PARTITION_LABEL)).trim();
         panelStatus.updatePartitionLabel(partitionId, label);
      } else if (msg.isFor(Message.ABSOLUTA_ZONE_LABEL)) {
         int zoneId = (Integer) msg.getParam(Message.ABSOLUTA_ZONE_LABEL);
         String label = ((String) msg.getValue(Message.ABSOLUTA_ZONE_LABEL)).trim();
         panelStatus.updateZoneLabel(zoneId, label);
      } else if (msg.isFor(Message.ABSOLUTA_OUTPUT_LABEL)) {
         int outputId = (Integer) msg.getParam(Message.ABSOLUTA_OUTPUT_LABEL);
         String label = ((String) msg.getValue(Message.ABSOLUTA_OUTPUT_LABEL)).trim();
         panelStatus.updateOutputLabel(outputId, label);
      } else if (msg.isFor(Message.ABSOLUTA_ARMING_MODE_LABEL)) {
         int armingModeId = (Integer) msg.getParam(Message.ABSOLUTA_ARMING_MODE_LABEL);
         String label = ((String) msg.getValue(Message.ABSOLUTA_ARMING_MODE_LABEL)).trim();
         panelStatus.updateArmingModeLabel(armingModeId, label);
      } else if (msg.isFor(Message.TROUBLE_DETAIL_NOTIFICATION)) {
         List<Quartet<Integer, Integer, Integer, Integer>> troubles =
            (List<Quartet<Integer, Integer, Integer, Integer>>) msg.getValue(Message.TROUBLE_DETAIL_NOTIFICATION);

         for (Quartet<Integer, Integer, Integer, Integer> trouble : troubles) {
            this.manageTrouble(trouble);
         }
      } else if (msg.isFor(Message.TROUBLE_DETAIL)) {
         Pair<Integer, Integer> param = (Pair<Integer, Integer>) msg.getParam(Message.TROUBLE_DETAIL);
         List<Integer> deviceNumbers = (List<Integer>) msg.getValue(Message.TROUBLE_DETAIL);
         int deviceType = param.getValue0();
         int troubleType = param.getValue1();
         for (Integer deviceNumber : deviceNumbers) {
            this.manageTrouble(Quartet.with(deviceType, troubleType, deviceNumber, Trouble.TROUBLE));
         }
      }
   }

   @Override
   public void error(DscError error) {
      // Optionally log error
   }

   private void updatePartitionStatus(int partitionId, List<Boolean> statusMask) {
      if (partitionStatusUpdateSkipCount < 20) {
         partitionStatusUpdateSkipCount++;
         return;
      }

      PanelStatus.PartitionArming armingMode;
      if (!statusMask.get(PARTITION_ARMED)) {
         armingMode = PanelStatus.PartitionArming.DISARMED;
      } else if (statusMask.get(PARTITION_AWAY)) {
         armingMode = PanelStatus.PartitionArming.AWAY;
      } else if (statusMask.get(PARTITION_STAY)) {
         armingMode = PanelStatus.PartitionArming.STAY;
      } else if (!statusMask.get(PARTITION_NODELAY) && !statusMask.get(PARTITION_NIGHT)) {
         logger.warning("Arming status ambiguous for partition " + partitionId);
         armingMode = PanelStatus.PartitionArming.AWAY;
      } else {
         armingMode = PanelStatus.PartitionArming.NODELAY;
      }

      // Partition status detection
      PanelStatus.PartitionStatus partitionStatus;
      if (statusMask.get(PARTITION_FIRE)) {
         partitionStatus = PanelStatus.PartitionStatus.FIRE;
      } else if (statusMask.get(PARTITION_TROUBLES)) {
         partitionStatus = PanelStatus.PartitionStatus.FAULTS;
      } else if (!statusMask.get(PARTITION_ALARM) && !statusMask.get(PARTITION_ALARM_IN_MEMORY)) {
         partitionStatus = PanelStatus.PartitionStatus.OK;
      } else {
         partitionStatus = PanelStatus.PartitionStatus.ALARMS;
      }

      panelStatus.updatePartitionStatus(partitionId, partitionStatus);
      panelStatus.updatePartitionAlarmInMemory(partitionId, statusMask.get(PARTITION_ALARM_IN_MEMORY));

      // Override to TRIGGERED only when the alarm is actively sounding, not just in memory.
      // PARTITION_ALARM_IN_MEMORY stays true after disarming and would otherwise keep the
      // partition stuck in TRIGGERED until the memory bit is cleared by re-arming.
      if (statusMask.get(PARTITION_ALARM)) {
         armingMode = PanelStatus.PartitionArming.TRIGGERED;
      }

      // Preserve ARMING/DISARMING until the panel confirms the transition, so intermediate
      // status packets (still showing old state) don't flash the UI back to the previous state.
      PanelStatus.PartitionArming currentArming = panelStatus.getPartitionArming(partitionId);
      if (currentArming == PanelStatus.PartitionArming.ARMING && armingMode == PanelStatus.PartitionArming.DISARMED) {
         armingMode = PanelStatus.PartitionArming.ARMING;
      } else if (currentArming == PanelStatus.PartitionArming.DISARMING &&
                 armingMode != PanelStatus.PartitionArming.DISARMED &&
                 armingMode != PanelStatus.PartitionArming.TRIGGERED) {
         armingMode = PanelStatus.PartitionArming.DISARMING;
      }

      boolean ready;
      if (armingMode == PanelStatus.PartitionArming.DISARMED) {
         ready = statusMask.size() > PARTITION_READY && statusMask.get(PARTITION_READY);
      } else if (armingMode == PanelStatus.PartitionArming.TRIGGERED) {
         ready = false;
      } else {
         ready = true;
      }
      panelStatus.updatePartitionReady(partitionId, ready);

      panelStatus.updatePartitionArming(partitionId, armingMode);

      boolean anyPartitionArmed = false;
      boolean anyPartitionDisarmed = false;
      boolean missingData = false;
      boolean anyPartitionTriggered = false;
      boolean anyPartitionArming = false;
      boolean anyPartitionDisarming = false;

      for (Integer id : panelStatus.getPartitions()) {
         PanelStatus.PartitionArming mode = panelStatus.getPartitionArming(id);
         if (mode == null) {
            missingData = true;
         } else if (mode == PanelStatus.PartitionArming.TRIGGERED) {
            anyPartitionTriggered = true;
         } else if (mode == PanelStatus.PartitionArming.ARMING) {
            anyPartitionArming = true;
         } else if (mode == PanelStatus.PartitionArming.DISARMING) {
            anyPartitionDisarming = true;
         } else if (mode == PanelStatus.PartitionArming.DISARMED) {
            anyPartitionDisarmed = true;
         } else {
            anyPartitionArmed = true;
         }
      }

      PanelStatus.GlobalArming globalArming;
      if (missingData) {
         globalArming = null;
      } else if (anyPartitionTriggered) {
         globalArming = PanelStatus.GlobalArming.TRIGGERED;
      } else if (anyPartitionArming) {
         globalArming = PanelStatus.GlobalArming.ARMING;
      } else if (anyPartitionDisarming) {
         globalArming = PanelStatus.GlobalArming.DISARMING;
      } else if (anyPartitionArmed && anyPartitionDisarmed) {
         globalArming = PanelStatus.GlobalArming.PARTIALLY_ARMED;
      } else if (anyPartitionArmed) {
         globalArming = PanelStatus.GlobalArming.GLOBALLY_ARMED;
      } else {
         globalArming = PanelStatus.GlobalArming.GLOBALLY_DISARMED;
      }

      // When Commander.arming() sends a system-level arm/disarm, individual partitions are not
      // set to ARMING/DISARMING. Preserve the global state so intermediate panel status packets
      // (still showing old state) don't override it before the panel confirms the transition.
      PanelStatus.GlobalArming currentGlobal = panelStatus.getGlobalArming();
      if (currentGlobal == PanelStatus.GlobalArming.ARMING && globalArming == PanelStatus.GlobalArming.GLOBALLY_DISARMED) {
         globalArming = PanelStatus.GlobalArming.ARMING;
      } else if (currentGlobal == PanelStatus.GlobalArming.DISARMING &&
                 (globalArming == PanelStatus.GlobalArming.GLOBALLY_ARMED ||
                  globalArming == PanelStatus.GlobalArming.PARTIALLY_ARMED)) {
         globalArming = PanelStatus.GlobalArming.DISARMING;
      }

      panelStatus.updateGlobalArming(globalArming);
   }

   private void updateZoneStatus(int zoneId, List<Boolean> statusMask) {
      PanelStatus.InputStatus zoneStatus;
      if (statusMask.get(ZONE_BYPASSED)) {
         zoneStatus = statusMask.get(ZONE_OPEN) ? PanelStatus.InputStatus.ACTIVE : PanelStatus.InputStatus.OK;
      } else if (!statusMask.get(ZONE_TAMPER) && !statusMask.get(ZONE_DELINQUENCY)) {
         if (!statusMask.get(ZONE_FAULT) && !statusMask.get(ZONE_LOW_BATTERY)) {
               if (!statusMask.get(ZONE_ALARM) && !statusMask.get(ZONE_ALARM_IN_MEMORY)) {
                  zoneStatus = statusMask.get(ZONE_OPEN) ? PanelStatus.InputStatus.ACTIVE : PanelStatus.InputStatus.OK;
               } else {
                  zoneStatus = PanelStatus.InputStatus.ALARM;
               }
         } else {
               zoneStatus = PanelStatus.InputStatus.FAULT;
         }
      } else {
         zoneStatus = PanelStatus.InputStatus.TAMPER;
      }

      panelStatus.updateZoneBypass(zoneId, statusMask.get(ZONE_BYPASSED));
      panelStatus.updateZoneStatus(zoneId, zoneStatus);
   }

   private void manageTrouble(Quartet<Integer, Integer, Integer, Integer> quartet2) {
      int deviceType = (Integer)quartet2.getValue0();
      int troubleType = (Integer)quartet2.getValue1();
      int deviceNumber = (Integer)quartet2.getValue2();
      int troubleStatus = (Integer)quartet2.getValue3();
      if (deviceType == 1 && !this.panelStatus.getZones().contains(deviceNumber)) {
         logger.fine("discarding trouble 0x" + troubleType + " notification for zone " + deviceNumber + " (the zone doesn't belong to the current user)");
      } else {
         Trouble trouble = new Trouble(deviceType, troubleType, deviceNumber, false);
         Trouble inMemoryTrouble = new Trouble(deviceType, troubleType, deviceNumber, true);
         switch (troubleStatus) {
            case 0:
               this.panelStatus.removeTrouble(trouble);
               this.panelStatus.removeTrouble(inMemoryTrouble);
               break;
            case 1:
               this.panelStatus.removeTrouble(inMemoryTrouble);
               this.panelStatus.addTrouble(trouble);
               break;
            case 2:
               this.panelStatus.removeTrouble(trouble);
               this.panelStatus.addTrouble(inMemoryTrouble);
         }

      }
   }
}