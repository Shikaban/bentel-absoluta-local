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

   void setSystemLabel(String var1) {
      String var2 = this.systemLabel;
      this.systemLabel = var1;
      this.changeSupport.firePropertyChange("SYSTEM_LABEL", var2, var1);
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

   void setPartitionArming(int var1, cms.device.api.Partition.Arming var2) {
      if (this.partitions.contains(var1)) {
         cms.device.api.Partition.Arming var3 = (cms.device.api.Partition.Arming)this.partitionArmings.get(var1);
         this.partitionArmings.put(var1, var2);
         this.changeSupport.fireIndexedPropertyChange("PARTITION_ARMING", var1, var3, var2);
      }

   }

   public cms.device.api.Partition.Arming getPartitionArming(int var1) {
      return (cms.device.api.Partition.Arming)this.partitionArmings.get(var1);
   }

   void setPartitionStatus(int var1, Status var2) {
      if (this.partitions.contains(var1)) {
         Status var3 = (Status)this.partitionStatuses.get(var1);
         this.partitionStatuses.put(var1, var2);
         this.changeSupport.fireIndexedPropertyChange("PARTITION_STATUS", var1, var3, var2);
      }

   }

   public Status getPartitionStatus(int var1) {
      return (Status)this.partitionStatuses.get(var1);
   }

   void setPartitionLabel(int var1, String var2) {
      if (this.partitions.contains(var1)) {
         String var3 = (String)this.partitionLabels.get(var1);
         this.partitionLabels.put(var1, var2);
         this.changeSupport.fireIndexedPropertyChange("PARTITION_LABEL", var1, var3, var2);
      }

   }

   public String getPartitionLabel(int var1) {
      return (String)this.partitionLabels.get(var1);
   }

   void setZoneStatus(int var1, cms.device.api.Input.Status var2) {
      if (this.zones.contains(var1)) {
         cms.device.api.Input.Status var3 = (cms.device.api.Input.Status)this.zoneStatuses.get(var1);
         this.zoneStatuses.put(var1, var2);
         this.changeSupport.fireIndexedPropertyChange("ZONE_STATUS", var1, var3, var2);
      }

   }

   public cms.device.api.Input.Status getZoneStatus(int var1) {
      return (cms.device.api.Input.Status)this.zoneStatuses.get(var1);
   }

   void setZoneBypass(int var1, Boolean var2) {
      if (this.zones.contains(var1)) {
         Boolean var3 = (Boolean)this.zoneBypass.get(var1);
         this.zoneBypass.put(var1, var2);
         this.changeSupport.fireIndexedPropertyChange("ZONE_BYPASS", var1, var3, var2);
      }

   }

   public Boolean getZoneBypass(int var1) {
      return (Boolean)this.zoneBypass.get(var1);
   }

   void setZoneLabel(int var1, String var2) {
      if (this.zones.contains(var1)) {
         String var3 = (String)this.zoneLabels.get(var1);
         this.zoneLabels.put(var1, var2);
         this.changeSupport.fireIndexedPropertyChange("ZONE_LABEL", var1, var3, var2);
      }

   }

   public String getZoneLabel(int var1) {
      return (String)this.zoneLabels.get(var1);
   }

   void setOutputStatus(int var1, cms.device.api.Output.Status var2) {
      if (this.outputs.contains(var1)) {
         cms.device.api.Output.Status var3 = (cms.device.api.Output.Status)this.outputStatuses.get(var1);
         this.outputStatuses.put(var1, var2);
         this.changeSupport.fireIndexedPropertyChange("OUTPUT_STATUS", var1, var3, var2);
      }

   }

   public cms.device.api.Output.Status getOutputStatus(int var1) {
      return (cms.device.api.Output.Status)this.outputStatuses.get(var1);
   }

   void setOutputLabel(int var1, String var2) {
      if (this.outputs.contains(var1)) {
         String var3 = (String)this.outputLabels.get(var1);
         this.outputLabels.put(var1, var2);
         this.changeSupport.fireIndexedPropertyChange("OUTPUT_LABEL", var1, var3, var2);
      }

   }

   public String getOutputLabel(int var1) {
      return (String)this.outputLabels.get(var1);
   }

   void setArmingModeLabel(int var1, String var2) {
      String var3 = (String)this.armingModeLabels.get(var1);
      this.armingModeLabels.put(var1, var2);
      this.changeSupport.fireIndexedPropertyChange("ARMING_MODE_LABEL", var1, var3, var2);
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
