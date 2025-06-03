
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
   private static final int PI_ARMED = 0;
   private static final int PI_STAY = 1;
   private static final int PI_AWAY = 2;
   private static final int PI_NIGHT = 3;
   private static final int PI_NODELAY = 4;
   private static final int PI_ALARM = 8;
   private static final int PI_TROUBLES = 9;
   private static final int PI_ALARM_IN_MEMORY = 12;
   private static final int PI_FIRE = 17;
   private static final int ZI_OPEN = 0;
   private static final int ZI_TAMPER = 1;
   private static final int ZI_FAULT = 2;
   private static final int ZI_LOW_BATTERY = 3;
   private static final int ZI_DELINQUENCY = 4;
   private static final int ZI_ALARM = 5;
   private static final int ZI_ALARM_IN_MEMORY = 6;
   private static final int ZI_BYPASSED = 7;
   private final PanelStatus panelStatus;

   StatusListener(PanelStatus var1) {
      this.panelStatus = (PanelStatus)Objects.requireNonNull(var1);
   }

   public void newValue(NewValue msg) {
      if (msg.isFor(Message.PARTITION_ASSIGNMENT_CONFIGURATION)) {
         List<Integer> partitions = (List<Integer>)msg.getValue(Message.PARTITION_ASSIGNMENT_CONFIGURATION);
         this.panelStatus.setPartitions(ImmutableList.copyOf(partitions));
      } else {
         if (msg.isFor(Message.PARTITION_ZONES)) {
            Integer partitionNumber = (Integer)msg.getParam(Message.PARTITION_ZONES);
            if (partitionNumber == null) {
               List<Integer> zones = (List<Integer>)msg.getValue(Message.PARTITION_ZONES);
               this.panelStatus.setZones(ImmutableList.copyOf(zones));
            } else {
               System.out.println("WARN: unexpected partition number for partition zones: " + partitionNumber);
            }
         } else if (msg.isFor(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)) {
            List<Integer> outputs = (List<Integer>) ((Pair<?, ?>) msg.getValue(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)).getValue0();
            this.panelStatus.setOutputs(ImmutableList.copyOf(outputs));
         } else if (msg.isFor(Message.PARTITION_STATUSES)) {
            List<Integer> partitionIds = (List<Integer>)msg.getParam(Message.PARTITION_STATUSES);
            List<List<Boolean>> partitionStatuses = (List<List<Boolean>>)msg.getValue(Message.PARTITION_STATUSES);

            assert partitionIds.size() == partitionStatuses.size();

            for(int i = 0; i < partitionIds.size(); ++i) {
               int partitionId = partitionIds.get(i);
               List<Boolean> statusMask = partitionStatuses.get(i);
               this.updatePartitionStatus(partitionId, statusMask);
            }
         } else {
            if (msg.isFor(Message.ZONE_STATUSES)) {
               int zoneId = (Integer) ((Pair<?, ?>) msg.getParam(Message.ZONE_STATUSES)).getValue0();
               List<List<Boolean>> zoneStatuses = (List<List<Boolean>>) msg.getValue(Message.ZONE_STATUSES);

               for(List<Boolean> statusMask : zoneStatuses) {
                  this.updateZoneStatus(zoneId, statusMask);
                  zoneId++;
               }
            } else if (msg.isFor(Message.ABSOLUTA_COMMAND_OUTPUT_ACTIVATION)) {
               List<Integer> activeOutputs = (List<Integer>) msg.getValue(Message.ABSOLUTA_COMMAND_OUTPUT_ACTIVATION);
               for (Integer outputId : this.panelStatus.getOutputs()) {
                  Status outputStatus = activeOutputs.contains(outputId) ? Status.CLOSED : Status.OPEN;
                  this.panelStatus.setOutputStatus(outputId, outputStatus);
               }
            } else if (msg.isFor(Message.ABSOLUTA_SYSTEM_LABEL)) {
               String systemLabel = ((String)msg.getValue(Message.ABSOLUTA_SYSTEM_LABEL)).trim();
               this.panelStatus.setSystemLabel(systemLabel);
            } else if (msg.isFor(Message.ABSOLUTA_PARTITION_LABEL)) {
               int partitionId = (Integer) msg.getParam(Message.ABSOLUTA_PARTITION_LABEL);
               String label = ((String) msg.getValue(Message.ABSOLUTA_PARTITION_LABEL)).trim();
               this.panelStatus.setPartitionLabel(partitionId, label);
            } else if (msg.isFor(Message.ABSOLUTA_ZONE_LABEL)) {
               int zoneId = (Integer) msg.getParam(Message.ABSOLUTA_ZONE_LABEL);
               String label = ((String) msg.getValue(Message.ABSOLUTA_ZONE_LABEL)).trim();
               this.panelStatus.setZoneLabel(zoneId, label);
            } else if (msg.isFor(Message.ABSOLUTA_OUTPUT_LABEL)) {
                  int outputId = (Integer) msg.getParam(Message.ABSOLUTA_OUTPUT_LABEL);
                  String label = ((String) msg.getValue(Message.ABSOLUTA_OUTPUT_LABEL)).trim();
                  this.panelStatus.setOutputLabel(outputId, label);
            } else if (msg.isFor(Message.ABSOLUTA_ARMING_MODE_LABEL)) {
               int armingModeId = (Integer) msg.getParam(Message.ABSOLUTA_ARMING_MODE_LABEL);
               String label = ((String) msg.getValue(Message.ABSOLUTA_ARMING_MODE_LABEL)).trim();
               this.panelStatus.setArmingModeLabel(armingModeId, label);
            }
         }
      }
   }

   public void error(DscError var1) {
   }

   private void updatePartitionStatus(int selectedPartitionID, List<Boolean> dataPartitionMask) {
      Arming newPartitionmode;
      if (!(Boolean)dataPartitionMask.get(PI_ARMED)) {
         newPartitionmode = Arming.DISARMED;
      } else if ((Boolean)dataPartitionMask.get(PI_AWAY)) {
         newPartitionmode = Arming.AWAY;
      } else if ((Boolean)dataPartitionMask.get(PI_STAY)) {
         newPartitionmode = Arming.STAY;
      } else if (!(Boolean)dataPartitionMask.get(PI_NODELAY) && !(Boolean)dataPartitionMask.get(PI_NIGHT)) {
         System.out.println("WARN: arming status");
         newPartitionmode = Arming.AWAY;
      } else {
         newPartitionmode = Arming.NODELAY;
      }

      // Rilevo lo stato della partizione
      cms.device.api.Partition.Status newPartitionStatus;
      if ((Boolean)dataPartitionMask.get(PI_FIRE)) {
         newPartitionStatus = cms.device.api.Partition.Status.FIRE;
      } else if ((Boolean)dataPartitionMask.get(PI_TROUBLES)) {
         newPartitionStatus = cms.device.api.Partition.Status.FAULTS;
      } else if (!(Boolean)dataPartitionMask.get(PI_ALARM) && !(Boolean)dataPartitionMask.get(PI_ALARM_IN_MEMORY)) {
         newPartitionStatus = cms.device.api.Partition.Status.OK;
      } else {
         newPartitionStatus = cms.device.api.Partition.Status.ALARMS;
      }

      this.panelStatus.setPartitionStatus(selectedPartitionID, newPartitionStatus);

      // Se la partizione è in allarme, imposto TRIGGERED
      if (newPartitionStatus == cms.device.api.Partition.Status.ALARMS){
         newPartitionmode = Arming.TRIGGERED;
      }

      this.panelStatus.setPartitionArming(selectedPartitionID, newPartitionmode);
      boolean systemArmed = false;
      boolean onePartitionDisarmed = false;
      boolean noValidData = false;

      for (Integer partitionId : this.panelStatus.getPartitions()) {
         Arming partitionMode = this.panelStatus.getPartitionArming(partitionId);
         if (partitionMode == null) {
            noValidData = true;
         } else if (partitionMode == Arming.DISARMED) {
            onePartitionDisarmed = true;
         } else {
            systemArmed = true;
         }
      }

      cms.device.api.Panel.Arming newGlobalMode;
      if (noValidData) {
         newGlobalMode = null;
      } else if (systemArmed && onePartitionDisarmed) {
         newGlobalMode = cms.device.api.Panel.Arming.PARTIALLY_ARMED;
      } else if (systemArmed) {
         newGlobalMode = cms.device.api.Panel.Arming.GLOBALLY_ARMED;
      } else {
         newGlobalMode = cms.device.api.Panel.Arming.GLOBALLY_DISARMED;
      }

      this.panelStatus.setGlobalArming(newGlobalMode);
   }

   private void updateZoneStatus(int zoneID, List<Boolean> var2) {
      cms.device.api.Input.Status newStatus;
      if ((Boolean)var2.get(ZI_BYPASSED)) {
         // Rilevo lo stesso lo stato della zona
         if ((Boolean)var2.get(ZI_OPEN)) {
               newStatus = cms.device.api.Input.Status.ACTIVE;
         } else {
            newStatus = cms.device.api.Input.Status.OK;
         }
      } else if (!(Boolean)var2.get(ZI_TAMPER) && !(Boolean)var2.get(ZI_DELINQUENCY)) {
         if (!(Boolean)var2.get(ZI_FAULT) && !(Boolean)var2.get(ZI_LOW_BATTERY)) {
            if (!(Boolean)var2.get(ZI_ALARM) && !(Boolean)var2.get(ZI_ALARM_IN_MEMORY)) {
               if ((Boolean)var2.get(ZI_OPEN)) {
                  newStatus = cms.device.api.Input.Status.ACTIVE;
               } else {
                  newStatus = cms.device.api.Input.Status.OK;
               }
            } else {
               newStatus = cms.device.api.Input.Status.ALARM;
            }
         } else {
            newStatus = cms.device.api.Input.Status.FAULT;
         }
      } else {
         newStatus = cms.device.api.Input.Status.TAMPER;
      }

      this.panelStatus.setZoneBypass(zoneID, (Boolean)var2.get(ZI_BYPASSED));
      this.panelStatus.setZoneStatus(zoneID, newStatus);
   }

}