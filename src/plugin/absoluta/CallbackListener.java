package plugin.absoluta;

import cms.device.spi.PanelProvider.PanelCallback;
import plugin.absoluta.connection.CustomizedArmingModes;
import plugin.absoluta.connection.PanelStatus;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class CallbackListener implements PropertyChangeListener {
   private final PanelCallback callback;
   private final PanelStatus panelStatus;

   CallbackListener(PanelCallback var1, PanelStatus var2) {
      this.callback = (PanelCallback)Objects.requireNonNull(var1);
      this.panelStatus = (PanelStatus)Objects.requireNonNull(var2);
   }

   public void propertyChange(PropertyChangeEvent property) {
      assert this.panelStatus == property.getSource();

      if (property.getNewValue() != null) {
         if (property instanceof IndexedPropertyChangeEvent) {
            int indexProperty = ((IndexedPropertyChangeEvent)property).getIndex();
            String indexProperyString = String.valueOf(indexProperty);
            String propertyName = property.getPropertyName();
            switch(propertyName) {
               case "PARTITION_ARMING":
                  this.callback.setPartitionArming(indexProperyString, this.panelStatus.getPartitionArming(indexProperty));
                  break;
               case "PARTITION_STATUS":
                  this.callback.setPartitionStatus(indexProperyString, this.panelStatus.getPartitionStatus(indexProperty));
                  break;
               case "PARTITION_LABEL":
                  this.callback.setPartitionRemoteName(indexProperyString, this.panelStatus.getPartitionLabel(indexProperty));
                  break;
               case "ZONE_STATUS":
               case "ZONE_BYPASS":
                  this.callback.setInputStatus(indexProperyString, this.panelStatus.getZoneStatus(indexProperty));
                  break;
               case "ZONE_LABEL":
                  this.callback.setInputRemoteName(indexProperyString, this.panelStatus.getZoneLabel(indexProperty));
                  break;
               case "OUTPUT_STATUS":
                  this.callback.setOutputStatus(indexProperyString, this.panelStatus.getOutputStatus(indexProperty));
                  break;
               case "OUTPUT_LABEL":
                  this.callback.setOutputRemoteName(indexProperyString, this.panelStatus.getOutputLabel(indexProperty));
                  break;
               case "ARMING_MODE_LABEL":
                  Character charMode = (Character)CustomizedArmingModes.ARMING_MODE_LABELS.get(indexProperty);
                  if (charMode != null) {
                     this.callback.setLabelArming(charMode, this.panelStatus.getArmingModeLabel(indexProperty));
                  }
                  break;
            }
         }
         else {
            String propertyName = property.getPropertyName();
            switch(propertyName) {
               case "CONNECTION_STATUS":
                  if (PanelStatus.ConnectionStatus.CONNECTED == property.getOldValue() && PanelStatus.ConnectionStatus.DISCONNECTED == property.getNewValue()) {
                     this.callback.connectionLost();
                  }
                  break;
               case "GLOBAL_ARMING":
                  this.callback.setArming(this.panelStatus.getGlobalArming());
                  break;
               case "SYSTEM_LABEL":
                  break;
               case "PARTITIONS":
                  this.callback.changePartitions(toStringList(this.panelStatus.getPartitions()));
                  break;
               case "ZONES":
                  List<String> zoneStrings = toStringList(this.panelStatus.getZones());
                  this.callback.changeInputs(zoneStrings);
                  List<String> partitionStrings = toStringList(this.panelStatus.getPartitions());
                  for (String partitionId : partitionStrings) {
                     this.callback.tagInputIntoPartition(partitionId, zoneStrings);
                  }
                  return;
               case "OUTPUTS":
                  this.callback.changeOutputs(toStringList(this.panelStatus.getOutputs()));
                  break;
               case "TROUBLES":
                  break;
            }
         }
      }
   }

   private static List<String> toStringList(List<Integer> intList) {
      List<String> stringList = new ArrayList<>(intList.size());
      for (Integer value : intList) {
         stringList.add(value.toString());
      }
      return stringList;
   }
}
