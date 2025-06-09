package plugin.absoluta.connection;

import com.google.common.collect.ImmutableList;
import cms.device.api.Output.Status;
import cms.device.api.Partition.Arming;
import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.NewValue;
import java.util.List;
import java.util.Objects;
import org.javatuples.Pair;

class StatusListener implements MessageListener {

   // Partition status indices
   private static final int PARTITION_ARMED = 0;
   private static final int PARTITION_STAY = 1;
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
         panelStatus.setPartitions(ImmutableList.copyOf(partitions));
      } else if (msg.isFor(Message.PARTITION_ZONES)) {
         Integer partitionNumber = (Integer) msg.getParam(Message.PARTITION_ZONES);
         if (partitionNumber == null) {
               List<Integer> zones = (List<Integer>) msg.getValue(Message.PARTITION_ZONES);
               panelStatus.setZones(ImmutableList.copyOf(zones));
         } else {
            System.out.println("WARN: unexpected partition number for partition zones: " + partitionNumber);
         }
      } else if (msg.isFor(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)) {
         List<Integer> outputs = (List<Integer>) ((Pair<?, ?>) msg.getValue(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)).getValue0();
         panelStatus.setOutputs(ImmutableList.copyOf(outputs));
      } else if (msg.isFor(Message.PARTITION_STATUSES)) {
         List<Integer> partitionIds = (List<Integer>) msg.getParam(Message.PARTITION_STATUSES);
         List<List<Boolean>> partitionStatuses = (List<List<Boolean>>) msg.getValue(Message.PARTITION_STATUSES);

            if (partitionIds.size() != partitionStatuses.size()) {
               System.out.println("WARN: Partition IDs and statuses size mismatch");
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
               Status outputStatus = activeOutputs.contains(outputId) ? Status.CLOSED : Status.OPEN;
               panelStatus.setOutputStatus(outputId, outputStatus);
         }
      } else if (msg.isFor(Message.ABSOLUTA_SYSTEM_LABEL)) {
         String systemLabel = ((String) msg.getValue(Message.ABSOLUTA_SYSTEM_LABEL)).trim();
         panelStatus.setSystemLabel(systemLabel);
      } else if (msg.isFor(Message.ABSOLUTA_PARTITION_LABEL)) {
         int partitionId = (Integer) msg.getParam(Message.ABSOLUTA_PARTITION_LABEL);
         String label = ((String) msg.getValue(Message.ABSOLUTA_PARTITION_LABEL)).trim();
         panelStatus.setPartitionLabel(partitionId, label);
      } else if (msg.isFor(Message.ABSOLUTA_ZONE_LABEL)) {
         int zoneId = (Integer) msg.getParam(Message.ABSOLUTA_ZONE_LABEL);
         String label = ((String) msg.getValue(Message.ABSOLUTA_ZONE_LABEL)).trim();
         panelStatus.setZoneLabel(zoneId, label);
      } else if (msg.isFor(Message.ABSOLUTA_OUTPUT_LABEL)) {
         int outputId = (Integer) msg.getParam(Message.ABSOLUTA_OUTPUT_LABEL);
         String label = ((String) msg.getValue(Message.ABSOLUTA_OUTPUT_LABEL)).trim();
         panelStatus.setOutputLabel(outputId, label);
      } else if (msg.isFor(Message.ABSOLUTA_ARMING_MODE_LABEL)) {
         int armingModeId = (Integer) msg.getParam(Message.ABSOLUTA_ARMING_MODE_LABEL);
         String label = ((String) msg.getValue(Message.ABSOLUTA_ARMING_MODE_LABEL)).trim();
         panelStatus.setArmingModeLabel(armingModeId, label);
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

      Arming armingMode;
      if (!statusMask.get(PARTITION_ARMED)) {
         armingMode = Arming.DISARMED;
      } else if (statusMask.get(PARTITION_AWAY)) {
         armingMode = Arming.AWAY;
      } else if (statusMask.get(PARTITION_STAY)) {
         armingMode = Arming.STAY;
      } else if (!statusMask.get(PARTITION_NODELAY) && !statusMask.get(PARTITION_NIGHT)) {
         System.out.println("WARN: Arming status ambiguous for partition " + partitionId);
         armingMode = Arming.AWAY;
      } else {
         armingMode = Arming.NODELAY;
      }

      // Partition status detection
      cms.device.api.Partition.Status partitionStatus;
      if (statusMask.get(PARTITION_FIRE)) {
         partitionStatus = cms.device.api.Partition.Status.FIRE;
      } else if (statusMask.get(PARTITION_TROUBLES)) {
         partitionStatus = cms.device.api.Partition.Status.FAULTS;
      } else if (!statusMask.get(PARTITION_ALARM) && !statusMask.get(PARTITION_ALARM_IN_MEMORY)) {
         partitionStatus = cms.device.api.Partition.Status.OK;
      } else {
         partitionStatus = cms.device.api.Partition.Status.ALARMS;
      }

      panelStatus.setPartitionStatus(partitionId, partitionStatus);

      // If partition is in alarm, set TRIGGERED
      if (partitionStatus == cms.device.api.Partition.Status.ALARMS) {
         armingMode = Arming.TRIGGERED;
      }

      panelStatus.setPartitionArming(partitionId, armingMode);

      boolean anyPartitionArmed = false;
      boolean anyPartitionDisarmed = false;
      boolean missingData = false;
      boolean anyPartitionTriggered = false;

      for (Integer id : panelStatus.getPartitions()) {
         Arming mode = panelStatus.getPartitionArming(id);
         if (mode == null) {
            missingData = true;
         } else if (mode == Arming.TRIGGERED){
            anyPartitionTriggered = true;
         }else if (mode == Arming.DISARMED) {
            anyPartitionDisarmed = true;
         } else {
            anyPartitionArmed = true;
         }
      }

      cms.device.api.Panel.Arming globalArming;
      if (missingData) {
         globalArming = null;
      } else if (anyPartitionTriggered) {
         globalArming = cms.device.api.Panel.Arming.TRIGGERED;
      } else if (anyPartitionArmed && anyPartitionDisarmed) {
         globalArming = cms.device.api.Panel.Arming.PARTIALLY_ARMED;
      } else if (anyPartitionArmed) {
         globalArming = cms.device.api.Panel.Arming.GLOBALLY_ARMED;
      } else {
         globalArming = cms.device.api.Panel.Arming.GLOBALLY_DISARMED;
      }

      panelStatus.setGlobalArming(globalArming);
   }

   private void updateZoneStatus(int zoneId, List<Boolean> statusMask) {
      cms.device.api.Input.Status zoneStatus;
      if (statusMask.get(ZONE_BYPASSED)) {
         zoneStatus = statusMask.get(ZONE_OPEN) ? cms.device.api.Input.Status.ACTIVE : cms.device.api.Input.Status.OK;
      } else if (!statusMask.get(ZONE_TAMPER) && !statusMask.get(ZONE_DELINQUENCY)) {
         if (!statusMask.get(ZONE_FAULT) && !statusMask.get(ZONE_LOW_BATTERY)) {
               if (!statusMask.get(ZONE_ALARM) && !statusMask.get(ZONE_ALARM_IN_MEMORY)) {
                  zoneStatus = statusMask.get(ZONE_OPEN) ? cms.device.api.Input.Status.ACTIVE : cms.device.api.Input.Status.OK;
               } else {
                  zoneStatus = cms.device.api.Input.Status.ALARM;
               }
         } else {
               zoneStatus = cms.device.api.Input.Status.FAULT;
         }
      } else {
         zoneStatus = cms.device.api.Input.Status.TAMPER;
      }

      panelStatus.setZoneBypass(zoneId, statusMask.get(ZONE_BYPASSED));
      panelStatus.setZoneStatus(zoneId, zoneStatus);
   }
}