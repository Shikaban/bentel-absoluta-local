package plugin.absoluta.connection;

import com.google.common.collect.ImmutableList;

import cms.device.api.Panel.Arming;
import cms.device.api.Partition.Status;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

public class PanelStatus {
   public static final String CONNECTION_STATUS = "CONNECTION_STATUS";
   public static final String GLOBAL_ARMING = "GLOBAL_ARMING";
   public static final String SYSTEM_LABEL = "SYSTEM_LABEL";
   public static final String PARTITIONS = "PARTITIONS";
   public static final String ZONES = "ZONES";
   public static final String OUTPUTS = "OUTPUTS";
   public static final String PARTITION_ARMING = "PARTITION_ARMING";
   public static final String PARTITION_LABEL = "PARTITION_LABEL";
   public static final String PARTITION_STATUS = "PARTITION_STATUS";
   public static final String ZONE_STATUS = "ZONE_STATUS";
   public static final String ZONE_BYPASS = "ZONE_BYPASS";
   public static final String ZONE_LABEL = "ZONE_LABEL";
   public static final String OUTPUT_STATUS = "OUTPUT_STATUS";
   public static final String OUTPUT_LABEL = "OUTPUT_LABEL";
   public static final String ARMING_MODE_LABEL = "ARMING_MODE_LABEL";
   public static final String TROUBLES = "TROUBLES";
   private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
   private PanelStatus.ConnectionStatus connectionStatus;
   private Arming globalArming;
   private String systemLabel;
   private ImmutableList<Integer> partitions;
   private ImmutableList<Integer> zones;
   private ImmutableList<Integer> outputs;
   private final Map<Integer, cms.device.api.Partition.Arming> partitionArmings;
   private final Map<Integer, Status> partitionStatuses;
   private final Map<Integer, String> partitionLabels;
   private final Map<Integer, cms.device.api.Input.Status> zoneStatuses;
   private final Map<Integer, Boolean> zoneBypass;
   private final Map<Integer, String> zoneLabels;
   private final Map<Integer, cms.device.api.Output.Status> outputStatuses;
   private final Map<Integer, String> outputLabels;
   private final Map<Integer, String> armingModeLabels;

   public PanelStatus() {
      this.connectionStatus = PanelStatus.ConnectionStatus.DISCONNECTED;
      this.partitionArmings = new HashMap();
      this.partitionStatuses = new HashMap();
      this.partitionLabels = new HashMap();
      this.zoneStatuses = new HashMap();
      this.zoneBypass = new HashMap();
      this.zoneLabels = new HashMap();
      this.outputStatuses = new HashMap();
      this.outputLabels = new HashMap();
      this.armingModeLabels = new HashMap();
   }

   public void addPropertyChangeListener(PropertyChangeListener var1) {
      this.changeSupport.addPropertyChangeListener(var1);
   }

   public void removePropertyChangeListener(PropertyChangeListener var1) {
      this.changeSupport.removePropertyChangeListener(var1);
   }

   void setConnectionStatus(PanelStatus.ConnectionStatus newConnectionStatus) {
      PanelStatus.ConnectionStatus oldConnectionStatus;
      synchronized(this) {
         oldConnectionStatus = this.connectionStatus;
         this.connectionStatus = newConnectionStatus;
      }
      this.changeSupport.firePropertyChange("CONNECTION_STATUS", oldConnectionStatus, newConnectionStatus);
   }

   public synchronized PanelStatus.ConnectionStatus getConnectionStatus() {
      return this.connectionStatus;
   }

   void setGlobalArming(Arming newMode) {
      Arming oldMode = this.globalArming;
      this.globalArming = newMode;
      this.changeSupport.firePropertyChange("GLOBAL_ARMING", oldMode, newMode);
   }

   public Arming getGlobalArming() {
      return this.globalArming;
   }

   void setSystemLabel(String newSystemLabel) {
      String oldSystemLabel = this.systemLabel;
      this.systemLabel = newSystemLabel;
      this.changeSupport.firePropertyChange("SYSTEM_LABEL", oldSystemLabel, newSystemLabel);
   }

   public String getSystemLabel() {
      return this.systemLabel;
   }

   void setPartitions(ImmutableList<Integer> var1) {
      ImmutableList<Integer> var2 = this.partitions;
      this.partitions = var1;
      this.changeSupport.firePropertyChange("PARTITIONS", var2, var1);
   }

   public ImmutableList<Integer> getPartitions() {
      return this.partitions;
   }

   void setZones(ImmutableList<Integer> var1) {
      ImmutableList<Integer> var2 = this.zones;
      this.zones = var1;
      this.changeSupport.firePropertyChange("ZONES", var2, var1);
   }

   public ImmutableList<Integer> getZones() {
      return this.zones;
   }

   void setOutputs(ImmutableList<Integer> var1) {
      ImmutableList<Integer> var2 = this.outputs;
      this.outputs = var1;
      this.changeSupport.firePropertyChange("OUTPUTS", var2, var1);
   }

   public ImmutableList<Integer> getOutputs() {
      return this.outputs;
   }

   void setPartitionArming(int partitionID, cms.device.api.Partition.Arming newMode) {
      if (this.partitions.contains(partitionID)) {
         cms.device.api.Partition.Arming oldMode = (cms.device.api.Partition.Arming)this.partitionArmings.get(partitionID);
         this.partitionArmings.put(partitionID, newMode);
         this.changeSupport.fireIndexedPropertyChange("PARTITION_ARMING", partitionID, oldMode, newMode);
      }

   }

   public cms.device.api.Partition.Arming getPartitionArming(int mode) {
      return (cms.device.api.Partition.Arming)this.partitionArmings.get(mode);
   }

   void setPartitionStatus(int partitionID, Status newStatus) {
      if (this.partitions.contains(partitionID)) {
         Status oldStatus = (Status)this.partitionStatuses.get(partitionID);
         this.partitionStatuses.put(partitionID, newStatus);
         this.changeSupport.fireIndexedPropertyChange("PARTITION_STATUS", partitionID, oldStatus, newStatus);
      }

   }

   public Status getPartitionStatus(int partitionID) {
      return (Status)this.partitionStatuses.get(partitionID);
   }

   void setPartitionLabel(int partitionID, String newLabel) {
      if (this.partitions.contains(partitionID)) {
         String oldLabel = (String)this.partitionLabels.get(partitionID);
         this.partitionLabels.put(partitionID, newLabel);
         this.changeSupport.fireIndexedPropertyChange("PARTITION_LABEL", partitionID, oldLabel, newLabel);
      }

   }

   public String getPartitionLabel(int partitionID) {
      return (String)this.partitionLabels.get(partitionID);
   }

   void setZoneStatus(int zoneID, cms.device.api.Input.Status newStatus) {
      if (this.zones.contains(zoneID)) {
         cms.device.api.Input.Status oldStatus = (cms.device.api.Input.Status)this.zoneStatuses.get(zoneID);
         this.zoneStatuses.put(zoneID, newStatus);
         this.changeSupport.fireIndexedPropertyChange("ZONE_STATUS", zoneID, oldStatus, newStatus);
      }

   }

   public cms.device.api.Input.Status getZoneStatus(int zoneID) {
      return (cms.device.api.Input.Status)this.zoneStatuses.get(zoneID);
   }

   void setZoneBypass(int zoneID, Boolean newSet) {
      if (this.zones.contains(zoneID)) {
         Boolean oldSet = (Boolean)this.zoneBypass.get(zoneID);
         this.zoneBypass.put(zoneID, newSet);
         this.changeSupport.fireIndexedPropertyChange("ZONE_BYPASS", zoneID, oldSet, newSet);
      }

   }

   public Boolean getZoneBypass(int zoneID) {
      return (Boolean)this.zoneBypass.get(zoneID);
   }

   void setZoneLabel(int zoneID, String newLabel) {
      if (this.zones.contains(zoneID)) {
         String oldLabel = (String)this.zoneLabels.get(zoneID);
         this.zoneLabels.put(zoneID, newLabel);
         this.changeSupport.fireIndexedPropertyChange("ZONE_LABEL", zoneID, oldLabel, newLabel);
      }

   }

   public String getZoneLabel(int zoneID) {
      return (String)this.zoneLabels.get(zoneID);
   }

   void setOutputStatus(int outputID, cms.device.api.Output.Status newStatus) {
      if (this.outputs.contains(outputID)) {
         cms.device.api.Output.Status oldStatus = (cms.device.api.Output.Status)this.outputStatuses.get(outputID);
         this.outputStatuses.put(outputID, newStatus);
         this.changeSupport.fireIndexedPropertyChange("OUTPUT_STATUS", outputID, oldStatus, newStatus);
      }

   }

   public cms.device.api.Output.Status getOutputStatus(int outputID) {
      return (cms.device.api.Output.Status)this.outputStatuses.get(outputID);
   }

   void setOutputLabel(int outputID, String newLabel) {
      if (this.outputs.contains(outputID)) {
         String oldLabel = (String)this.outputLabels.get(outputID);
         this.outputLabels.put(outputID, newLabel);
         this.changeSupport.fireIndexedPropertyChange("OUTPUT_LABEL", outputID, oldLabel, newLabel);
      }

   }

   public String getOutputLabel(int outputID) {
      return (String)this.outputLabels.get(outputID);
   }

   void setArmingModeLabel(int var1, String newLabel) {
      String oldLabel = (String)this.armingModeLabels.get(var1);
      this.armingModeLabels.put(var1, newLabel);
      this.changeSupport.fireIndexedPropertyChange("ARMING_MODE_LABEL", var1, oldLabel, newLabel);
   }

   public String getArmingModeLabel(int var1) {
      return (String)this.armingModeLabels.get(var1);
   }

   public static enum ConnectionStatus {
      CONNECTED,
      DISCONNECTING,
      DISCONNECTED;
   }
}
