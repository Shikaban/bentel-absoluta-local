package absoluta;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import absoluta.connection.Trouble;

import absoluta.AbsolutaPanelProvider.providerStatus;
import absoluta.connection.CustomizedArmingModes;
import absoluta.connection.PanelStatus;
import absoluta.spi.PanelProvider.PanelCallback;

class CallbackListener implements PropertyChangeListener {
   private static final Logger logger = Logger.getLogger(CallbackListener.class.getName());
   private final PanelCallback callback;
   private final PanelStatus panelStatus;

   CallbackListener(PanelCallback panelCallback2, PanelStatus panelStatus2) {
      this.callback = (PanelCallback)Objects.requireNonNull(panelCallback2);
      this.panelStatus = (PanelStatus)Objects.requireNonNull(panelStatus2);
   }

   public void propertyChange(PropertyChangeEvent property) {
      assert this.panelStatus == property.getSource();

      if (property.getNewValue() != null) {
         if (property instanceof IndexedPropertyChangeEvent) {
            int indexProperty = ((IndexedPropertyChangeEvent)property).getIndex();
            String propertyName = property.getPropertyName();
            switch(propertyName) {
               case "PARTITION_ARMING":
                  this.callback.updatePartitionArming(indexProperty, this.panelStatus.getPartitionArming(indexProperty));
                  break;
               case "PARTITION_STATUS":
                  this.callback.updatePartitionStatus(indexProperty, this.panelStatus.getPartitionStatus(indexProperty));
                  break;
               case "PARTITION_LABEL":
                  this.callback.updatePartitionName(indexProperty, this.panelStatus.getPartitionLabel(indexProperty));
                  break;
               case "ZONE_STATUS":
                  this.callback.updateZoneStatus(indexProperty, this.panelStatus.getZoneStatus(indexProperty));
                  break;
               case "ZONE_BYPASS":
                  this.callback.updateZoneBypass(indexProperty, this.panelStatus.getZoneBypass(indexProperty));
                  break;
               case "ZONE_LABEL":
                  this.callback.updateZoneName(indexProperty, this.panelStatus.getZoneLabel(indexProperty));
                  break;
               case "OUTPUT_STATUS":
                  this.callback.updateOutputStatus(indexProperty, this.panelStatus.getOutputStatus(indexProperty));
                  break;
               case "OUTPUT_LABEL":
                  this.callback.updateOutputName(indexProperty, this.panelStatus.getOutputLabel(indexProperty));
                  break;
               case "ARMING_MODE_LABEL":
                  Character charMode = (Character)CustomizedArmingModes.ARMING_MODE_LABELS.get(indexProperty);
                  if (charMode != null) {
                     this.callback.updateModeLabel(charMode, this.panelStatus.getArmingModeLabel(indexProperty));
                  }
                  break;
               case "PARTITION_ALARM_IN_MEMORY":
                  this.callback.updateAlarmMemory(buildAlarmMemoryList());
                  break;
            }
         }
         else {
            String propertyName = property.getPropertyName();
            switch(propertyName) {
               case "CONNECTION_STATUS":
                  if (PanelStatus.PanelConnStatus.CONNECTED == property.getOldValue() && PanelStatus.PanelConnStatus.DISCONNECTED == property.getNewValue()) {
                     this.callback.connectionLost();
                  }
                  break;
               case "GLOBAL_ARMING":
                  this.callback.updateGlobalArming(this.panelStatus.getGlobalArming());
                  break;
               case "SYSTEM_LABEL":
                  break;
               case "PARTITIONS":
                  this.callback.getAllPartitions(this.panelStatus.getPartitions());
                  break;
               case "ZONES":
                  List<Integer> zones = this.panelStatus.getZones();
                  this.callback.getAllZones(zones);
                  List<Integer> partitions = this.panelStatus.getPartitions();
                  for (int partitionId : partitions) {
                     this.callback.tagZoneIntoPartition(partitionId, zones);
                  }
                  return;
               case "OUTPUTS":
                  this.callback.getAllOutputs(this.panelStatus.getOutputs());
                  break;
               case "TROUBLES":
                  List<String> troubleStrings = new ArrayList<>();
                  for (Object obj : (Iterable<?>) property.getNewValue()) {
                     Trouble t = (Trouble) obj;
                     troubleStrings.add(t.toLocalizedString(this.panelStatus));
                  }
                  Collections.sort(troubleStrings);
                  this.callback.updatePanelFaults(Collections.unmodifiableList(troubleStrings));
                  this.callback.setStatus(troubleStrings.isEmpty() ? providerStatus.OK : providerStatus.FAULT);
                  break;
            }
         }
      }
   }

   private List<String> buildAlarmMemoryList() {
      List<String> alarmMemory = new ArrayList<>();
      List<Integer> partitions = this.panelStatus.getPartitions();
      if (partitions == null) return Collections.emptyList();
      for (Integer id : partitions) {
         Boolean inMemory = this.panelStatus.getPartitionAlarmInMemory(id);
         if (Boolean.TRUE.equals(inMemory)) {
            String label = this.panelStatus.getPartitionLabel(id);
            alarmMemory.add(label != null && !label.isEmpty() ? label : "Partizione " + id);
         }
      }
      return Collections.unmodifiableList(alarmMemory);
   }
}