package absoluta.connection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class PanelStatus {
   private static final Logger logger = Logger.getLogger(StatusListener.class.getName());

   // Costanti per i nomi delle proprietà osservabili
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
   public static final String PARTITION_READY_STATUS = "PARTITION_READY_STATUS";
   public static final String PARTITION_ALARM_IN_MEMORY = "PARTITION_ALARM_IN_MEMORY";

   private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

   private PanelConnStatus connectionStatus;
   private GlobalArming globalArming;
   private String systemLabel;
   private ImmutableList<Integer> partitions;
   private ImmutableList<Integer> zones;
   private ImmutableList<Integer> outputs;

   // Mappe per stato e label di partizioni, zone e uscite
   private final Map<Integer, PartitionArming> partitionArmings;
   private final Map<Integer, PartitionStatus> partitionStatuses;
   private final Map<Integer, String> partitionLabels;
   private final Map<Integer, InputStatus> zoneStatuses;
   private final Map<Integer, Boolean> zoneBypass;
   private final Map<Integer, String> zoneLabels;
   private final Map<Integer, OutputStatus> outputStatuses;
   private final Map<Integer, String> outputLabels;
   private final Map<Integer, String> armingModeLabels;
   private ImmutableSet<Trouble> troubles;
   private final Set<Trouble> unconfirmedTroubles;
   private final Map<Integer, Boolean> partitionReady = new HashMap<>();
   private final Map<Integer, Boolean> partitionAlarmInMemory = new HashMap<>();
   private List<Integer> pendingArmPartitions;

   public PanelStatus() {
      this.connectionStatus = PanelConnStatus.DISCONNECTED;
      this.partitionArmings = new HashMap<>();
      this.partitionStatuses = new HashMap<>();
      this.partitionLabels = new HashMap<>();
      this.zoneStatuses = new HashMap<>();
      this.zoneBypass = new HashMap<>();
      this.zoneLabels = new HashMap<>();
      this.outputStatuses = new HashMap<>();
      this.outputLabels = new HashMap<>();
      this.armingModeLabels = new HashMap<>();
      this.troubles = ImmutableSet.of();
      this.unconfirmedTroubles = new HashSet<>();
   }

   public void addPropertyChangeListener(PropertyChangeListener listener) {
      this.changeSupport.addPropertyChangeListener(listener);
   }

   public void removePropertyChangeListener(PropertyChangeListener listener) {
      this.changeSupport.removePropertyChangeListener(listener);
   }

   // Stato connessione pannello
   void updateConnectionStatus(PanelConnStatus newStatus) {
      PanelConnStatus oldStatus;
      synchronized (this) {
         oldStatus = this.connectionStatus;
         this.connectionStatus = newStatus;
      }
      this.changeSupport.firePropertyChange(CONNECTION_STATUS, oldStatus, newStatus);
   }

   public synchronized PanelConnStatus getConnectionStatus() {
      return this.connectionStatus;
   }

   // Stato armamento globale
   void updateGlobalArming(GlobalArming newMode) {
      GlobalArming oldMode = this.globalArming;
      this.globalArming = newMode;
      this.changeSupport.firePropertyChange(GLOBAL_ARMING, oldMode, newMode);
   }

   public GlobalArming getGlobalArming() {
      return this.globalArming;
   }

   void updateSystemLabel(String newLabel) {
      String oldLabel = this.systemLabel;
      this.systemLabel = newLabel;
      this.changeSupport.firePropertyChange(SYSTEM_LABEL, oldLabel, newLabel);
   }

   public String getSystemLabel() {
      return this.systemLabel;
   }

   void updatePartitions(ImmutableList<Integer> partitions) {
      ImmutableList<Integer> oldPartitions = this.partitions;
      this.partitions = partitions;
      this.changeSupport.firePropertyChange(PARTITIONS, oldPartitions, partitions);
   }

   public ImmutableList<Integer> getPartitions() {
      return this.partitions;
   }

   void updateZones(ImmutableList<Integer> zones) {
      ImmutableList<Integer> oldZones = this.zones;
      this.zones = zones;
      this.changeSupport.firePropertyChange(ZONES, oldZones, zones);
   }

   public ImmutableList<Integer> getZones() {
      return this.zones;
   }

   void updateOutputs(ImmutableList<Integer> outputs) {
      ImmutableList<Integer> oldOutputs = this.outputs;
      this.outputs = outputs;
      this.changeSupport.firePropertyChange(OUTPUTS, oldOutputs, outputs);
   }

   public ImmutableList<Integer> getOutputs() {
      return this.outputs;
   }

   // Stato armamento partizione
   void updatePartitionArming(int partitionId, PartitionArming newMode) {
      if (this.partitions != null && this.partitions.contains(partitionId)) {
         PartitionArming oldMode = this.partitionArmings.get(partitionId);
         this.partitionArmings.put(partitionId, newMode);
         this.changeSupport.fireIndexedPropertyChange(PARTITION_ARMING, partitionId, oldMode, newMode);
      }
   }

   public PartitionArming getPartitionArming(int partitionId) {
      return this.partitionArmings.get(partitionId);
   }

   // Stato partizione
   void updatePartitionStatus(int partitionId, PartitionStatus newStatus) {
      if (this.partitions != null && this.partitions.contains(partitionId)) {
         PartitionStatus oldStatus = this.partitionStatuses.get(partitionId);
         this.partitionStatuses.put(partitionId, newStatus);
         this.changeSupport.fireIndexedPropertyChange(PARTITION_STATUS, partitionId, oldStatus, newStatus);
      }
   }

   public PartitionStatus getPartitionStatus(int partitionId) {
      return this.partitionStatuses.get(partitionId);
   }

   // Label partizione
   void updatePartitionLabel(int partitionId, String newLabel) {
      if (this.partitions != null && this.partitions.contains(partitionId)) {
         String oldLabel = this.partitionLabels.get(partitionId);
         this.partitionLabels.put(partitionId, newLabel);
         this.changeSupport.fireIndexedPropertyChange(PARTITION_LABEL, partitionId, oldLabel, newLabel);
      }
   }

   public String getPartitionLabel(int partitionId) {
      return this.partitionLabels.get(partitionId);
   }

   // Stato zona
   void updateZoneStatus(int zoneId, InputStatus newStatus) {
      if (this.zones != null && this.zones.contains(zoneId)) {
         InputStatus oldStatus = this.zoneStatuses.get(zoneId);
         this.zoneStatuses.put(zoneId, newStatus);
         this.changeSupport.fireIndexedPropertyChange(ZONE_STATUS, zoneId, oldStatus, newStatus);
      }
   }

   public InputStatus getZoneStatus(int zoneId) {
      return this.zoneStatuses.get(zoneId);
   }

   // Bypass zona
   void updateZoneBypass(int zoneId, Boolean newSet) {
      if (this.zones != null && this.zones.contains(zoneId)) {
         Boolean oldSet = this.zoneBypass.get(zoneId);
         this.zoneBypass.put(zoneId, newSet);
         this.changeSupport.fireIndexedPropertyChange(ZONE_BYPASS, zoneId, oldSet, newSet);
      }
   }

   public Boolean getZoneBypass(int zoneId) {
      return this.zoneBypass.get(zoneId);
   }

   // Label zona
   void updateZoneLabel(int zoneId, String newLabel) {
      if (this.zones != null && this.zones.contains(zoneId)) {
         String oldLabel = this.zoneLabels.get(zoneId);
         this.zoneLabels.put(zoneId, newLabel);
         this.changeSupport.fireIndexedPropertyChange(ZONE_LABEL, zoneId, oldLabel, newLabel);
      }
   }

   public String getZoneLabel(int zoneId) {
      return this.zoneLabels.get(zoneId);
   }

   // Stato uscita
   void updateOutputStatus(int outputId, OutputStatus newStatus) {
      if (this.outputs != null && this.outputs.contains(outputId)) {
         OutputStatus oldStatus = this.outputStatuses.get(outputId);
         this.outputStatuses.put(outputId, newStatus);
         this.changeSupport.fireIndexedPropertyChange(OUTPUT_STATUS, outputId, oldStatus, newStatus);
      }
   }

   public OutputStatus getOutputStatus(int outputId) {
      return this.outputStatuses.get(outputId);
   }

   // Label uscita
   void updateOutputLabel(int outputId, String newLabel) {
      if (this.outputs != null && this.outputs.contains(outputId)) {
         String oldLabel = this.outputLabels.get(outputId);
         this.outputLabels.put(outputId, newLabel);
         this.changeSupport.fireIndexedPropertyChange(OUTPUT_LABEL, outputId, oldLabel, newLabel);
      }
   }

   public String getOutputLabel(int outputId) {
      return this.outputLabels.get(outputId);
   }

   // Stato prontezza all'armamento (valido solo quando disarmata)
   void updatePartitionReady(int partitionId, boolean ready) {
      if (this.partitions != null && this.partitions.contains(partitionId)) {
         Boolean old = this.partitionReady.get(partitionId);
         this.partitionReady.put(partitionId, ready);
         this.changeSupport.fireIndexedPropertyChange(PARTITION_READY_STATUS, partitionId, old, (Boolean) ready);
      }
   }

   public Boolean getPartitionReady(int partitionId) {
      return this.partitionReady.get(partitionId);
   }

   void updatePartitionAlarmInMemory(int partitionId, boolean inMemory) {
      if (this.partitions != null && this.partitions.contains(partitionId)) {
         Boolean old = this.partitionAlarmInMemory.get(partitionId);
         this.partitionAlarmInMemory.put(partitionId, inMemory);
         this.changeSupport.fireIndexedPropertyChange(PARTITION_ALARM_IN_MEMORY, partitionId, old, (Boolean) inMemory);
      }
   }

   public Boolean getPartitionAlarmInMemory(int partitionId) {
      return this.partitionAlarmInMemory.get(partitionId);
   }

   void setPendingArmPartitions(List<Integer> partitionIds) {
      this.pendingArmPartitions = partitionIds != null ? new ArrayList<>(partitionIds) : null;
   }

   public List<Integer> getPendingArmPartitions() {
      return this.pendingArmPartitions;
   }

   // Label modalità armamento
   void updateArmingModeLabel(int modeId, String newLabel) {
      String oldLabel = this.armingModeLabels.get(modeId);
      this.armingModeLabels.put(modeId, newLabel);
      this.changeSupport.fireIndexedPropertyChange(ARMING_MODE_LABEL, modeId, oldLabel, newLabel);
   }

   public String getArmingModeLabel(int modeId) {
      return this.armingModeLabels.get(modeId);
   }

   // Enum per stato connessione pannello
   public enum PanelConnStatus {
      CONNECTED("connected"),
      DISCONNECTING("disconnecting"),
      DISCONNECTED("disconnected");

      private final String wireValue;

      PanelConnStatus(String wireValue) {
         this.wireValue = wireValue;
      }

      @Override
      public String toString() {
         return this.wireValue;
      }
   }

   // Enum per stato zona
   public enum InputStatus {
      TAMPER("tamper"),
      FAULT("fault"),
      ALARM("alarm"),
      ACTIVE("active"),
      BYPASSED("bypassed"),
      OK("ok");

      private final String wireValue;

      InputStatus(String wireValue) {
         this.wireValue = wireValue;
      }

      @Override
      public String toString() {
         return this.wireValue;
      }
   }

   // Enum per stato uscita
   public enum OutputStatus {
      UNKNOWN("unknown"),
      OPEN("open"),
      CLOSED("closed");

      private final String wireValue;

      OutputStatus(String wireValue) {
         this.wireValue = wireValue;
      }

      @Override
      public String toString() {
         return this.wireValue;
      }
   }

   // Enum per modalità armamento partizione
   public enum PartitionArming {
      DISARMED("disarmed"),
      AWAY("armed_away"),
      STAY("armed_home"),
      NODELAY("armed_night"),
      TRIGGERED("triggered"),
      NOT_AVAILABLE("not_available"),
      ARMING("arming"),
      DISARMING("disarming");

      private final String wireValue;

      PartitionArming(String wireValue) {
         this.wireValue = wireValue;
      }

      @Override
      public String toString() {
         return this.wireValue;
      }
   }

   // Enum per stato partizione
   public enum PartitionStatus {
      FIRE("Fire"),
      TAMPER("Tamper"),
      FAULTS("Faults"),
      ALARMS("Alarms"),
      ACTIVE("Active"),
      OK("Ok");

      private final String wireValue;

      PartitionStatus(String wireValue) {
         this.wireValue = wireValue;
      }

      @Override
      public String toString() {
         return this.wireValue;
      }
   }

   // Enum per azione uscita
   public enum OutputAction {
      DO_IMPULSE("impulse"),
      DO_OPEN("open"),
      DO_CLOSE("close");

      private final String wireValue;

      OutputAction(String wireValue) {
         this.wireValue = wireValue;
      }

      @Override
      public String toString() {
         return this.wireValue;
      }
   }

   // Enum per stato armamento globale
   public enum GlobalArming {
      GLOBALLY_ARMED("armed_away"),
      PARTIALLY_ARMED("armed_custom_bypass"),
      GLOBALLY_DISARMED("disarmed"),
      TRIGGERED("triggered"),
      NOT_AVAILABLE("not_available"),
      ARMING("arming"),
      DISARMING("disarming");

      private final String wireValue;

      GlobalArming(String wireValue) {
         this.wireValue = wireValue;
      }

      @Override
      public String toString() {
         return this.wireValue;
      }
   }

   void addTrouble(Trouble trouble2) {
      if (this.unconfirmedTroubles.remove(trouble2)) {
         logger.fine("trouble confirmed: " + trouble2);
      }

      if (!this.troubles.contains(trouble2)) {
         ImmutableSet<Trouble> immutableSet2 = this.troubles;
         this.troubles = ImmutableSet.<Trouble>builder().addAll(this.troubles).add(trouble2).build();
         logger.fine("trouble added: " + trouble2);
         this.changeSupport.firePropertyChange("TROUBLES", immutableSet2, this.troubles);
      }

   }

   void removeTrouble(Trouble trouble2) {
      this.unconfirmedTroubles.remove(trouble2);
      if (this.troubles.contains(trouble2)) {
         Set<Trouble> hashSet2 = new HashSet<>(this.troubles);
         hashSet2.remove(trouble2);
         ImmutableSet<Trouble> immutableSet2 = this.troubles;
         this.troubles = ImmutableSet.copyOf(hashSet2);
         logger.fine("trouble removed: " + trouble2);
         this.changeSupport.firePropertyChange("TROUBLES", immutableSet2, this.troubles);
      }

   }

   boolean unconfirmAllTroubles() {
      this.unconfirmedTroubles.addAll(this.troubles);
      return !this.unconfirmedTroubles.isEmpty();
   }

   void removeUnconfirmedTroubles() {
      UnmodifiableIterator<Trouble> unmodifiableIterator2 = ImmutableSet.copyOf(this.unconfirmedTroubles).iterator();

      while(unmodifiableIterator2.hasNext()) {
         Trouble trouble2 = (Trouble)unmodifiableIterator2.next();
         logger.fine("unconfirmed trouble: " + trouble2);
         this.removeTrouble(trouble2);
      }

   }

   public ImmutableSet<Trouble> getTroubles() {
      return this.troubles;
   }
}