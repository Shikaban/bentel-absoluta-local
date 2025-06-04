package cms.device.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import cms.device.spi.PanelProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.ChangeSupport;

public final class Panel implements DeviceOrPanel {
   private boolean connected;
   private Arming arming;
   public Status status;
   private boolean alarmed;
   private final PanelProvider provider;
   private final ChangeSupport changeSupport;
   private final Map<Character, String> labelArming;
   private final Map<String, Partition> partitions;
   private final Map<String, Input> inputs;
   final OutputSupport outputSupport;

   public Panel(PanelProvider provider) {
      this.arming = Arming.NOT_AVAILABLE;
      this.status = Status.OK;
      this.connected = false;
      this.alarmed = false;
      this.changeSupport = new ChangeSupport(this);
      this.labelArming = new LinkedHashMap<>();
      this.partitions = new LinkedHashMap<>();
      this.inputs = new LinkedHashMap<>();
      this.outputSupport = new OutputSupport(this, this::doOutputAction);
      this.provider = provider;
      provider.initialize(new Callback());
   }

   public ConnStatus connect() {
      if (this.connected) {
         return ConnStatus.SUCCESS;
      } else {
         ConnStatus result = this.provider.connect();
         if (result != ConnStatus.SUCCESS) {
               return result;
         } else {
               this.connected = true;
               this.fireChange();
               return ConnStatus.SUCCESS;
         }
      }
   }

   public void disconnect() {
      if (this.connected) {
         this.provider.disconnect();
         this.connected = false;
         this.fireChange();
      }
   }

   public boolean isConnected() {
      return this.connected;
   }

   public boolean isAlarmed() {
      return this.alarmed;
   }

   public void setAlarmed(boolean alarmed) {
      if (this.alarmed != alarmed) {
         this.alarmed = alarmed;
         this.changeSupport.fireChange();
      }
   }

   public void arming(Arming mode) {
      this.provider.arming(mode);
   }

   public Arming getArming() {
      return this.arming;
   }

   void setArming(Arming newMode) {
      if (this.arming != newMode) {
         this.arming = newMode;
         this.changeSupport.fireChange();
      }
   }

   public Status getStatus() {
      return this.status;
   }

   void setStatus(Status newStatus) {
      if (this.status != newStatus) {
         this.status = newStatus;
         this.changeSupport.fireChange();
      }
   }

   public void fireChange() {
      this.changeSupport.fireChange();
      for (Partition partition : this.getPartitions().values()) {
         partition.fireChange();
      }
      for (Input input : this.getInputs().values()) {
         input.fireChange();
      }
   }

   void doChangePartitions(Iterable<String> newPartitionIds) {
      ImmutableList<String> currentPartitionIds = ImmutableList.copyOf(this.partitions.keySet());
      if (!Iterables.elementsEqual(currentPartitionIds, newPartitionIds)) {
         LinkedHashMap<String, Partition> updatedPartitions = Maps.newLinkedHashMap();
         for (String partitionId : newPartitionIds) {
               if (this.partitions.containsKey(partitionId)) {
                  updatedPartitions.put(partitionId, this.partitions.remove(partitionId));
               } else {
                  updatedPartitions.put(partitionId, new Partition(this));
               }
         }
         this.partitions.clear();
         this.partitions.putAll(updatedPartitions);
         this.fireChange();
      }
   }

   public Map<String, Partition> getPartitions() {
      return this.partitions;
   }

   public void partitionArming(String partitionId, Partition.Arming armingMode) {
      this.provider.partitionArming(partitionId, armingMode);
   }

   private void doChangeInputs(Iterable<String> newInputIds) {
      ImmutableList<String> currentInputIds = ImmutableList.copyOf(this.inputs.keySet());
      if (!Iterables.elementsEqual(currentInputIds, newInputIds)) {
         LinkedHashMap<String, Input> updatedInputs = Maps.newLinkedHashMap();
         for (String inputId : newInputIds) {
               if (this.inputs.containsKey(inputId)) {
                  updatedInputs.put(inputId, this.inputs.remove(inputId));
               } else {
                  updatedInputs.put(inputId, new Input(this));
               }
         }
         this.inputs.clear();
         this.inputs.putAll(updatedInputs);
         this.fireChange();
      }
   }

   public Map<String, Input> getInputs() {
      return this.inputs;
   }

   public void bypassInput(String zoneId, boolean setBypassed) {
      this.provider.setBypassed(zoneId, setBypassed);
   }

   public boolean getBypassInput(String zoneId) {
      return this.provider.getBypassed(zoneId);
   }

   private void doOutputAction(String outputId, Output.Action action) {
      this.provider.doOutputAction(outputId, action);
   }

   public boolean checkArmingSupport(char mode) {
      return this.provider.armingSupport(mode);
   }

   public void modalityArming(char mode) {
      this.provider.armingSet(mode);
   }

   public String getLabelArming(char mode) {
      return this.labelArming.get(mode);
   }

   void setLabelArming(char mode, String label) {
      this.labelArming.put(mode, label);
   }

   public Map<String, Output> getOutputs() {
      return this.outputSupport.getOutputs();
   }

   public enum Arming {
      GLOBALLY_ARMED,
      PARTIALLY_ARMED,
      GLOBALLY_DISARMED,
      NOT_AVAILABLE
   }

   public enum Status {
      TAMPER,
      FAULT,
      OK
   }

   public enum ConnStatus {
      USER_DISCONNECTED,
      SUCCESS,
      INCOMPATIBLE,
      UNAUTHORIZED,
      UNREACHABLE
   }

   private class Callback implements PanelProvider.PanelCallback {

      public void setArming(Arming arming) {
         Panel.this.setArming(arming);
      }

      public void setStatus(Status status) {
         Panel.this.setStatus(status);
      }

      public void changePartitions(List<String> partitionIds) {
         Panel.this.doChangePartitions(partitionIds);
      }

      public void setPartitionRemoteName(String partitionId, String remoteName) {
         Panel.this.getPartitions().get(partitionId).setRemoteName(remoteName);
      }

      public void setPartitionsArming(Partition.Arming arming) {
         for (Partition partition : Panel.this.getPartitions().values()) {
               partition.setArming(arming);
         }
      }

      public void setPartitionArming(String partitionId, Partition.Arming arming) {
         Panel.this.getPartitions().get(partitionId).setArming(arming);
      }

      public void setPartitionStatus(String partitionId, Partition.Status status) {
         Panel.this.getPartitions().get(partitionId).setStatus(status);
      }

      public void changeInputs(List<String> inputIds) {
         Panel.this.doChangeInputs(inputIds);
      }

      public void setInputRemoteName(String inputId, String remoteName) {
         Panel.this.getInputs().get(inputId).setRemoteName(remoteName);
      }

      public void setInputStatus(String inputId, Input.Status status) {
         Panel.this.getInputs().get(inputId).setStatus(status);
      }

      public void tagInputIntoPartition(String partitionId, List<String> inputIds) {
         Panel.this.getPartitions().get(partitionId).addInputs(inputIds);
      }

      public void changeOutputs(List<String> outputIds) {
         Panel.this.outputSupport.changeOutputs(outputIds);
      }

      public void setOutputRemoteName(String outputId, String remoteName) {
         Panel.this.getOutputs().get(outputId).setRemoteName(remoteName);
      }

      public void setOutputStatus(String outputId, Output.Status status) {
         Panel.this.getOutputs().get(outputId).setStatus(status);
      }

      public void connectionLost() {
         System.out.println("INFO: connection lost on: " + Panel.this);
         Panel.this.disconnect();
      }

      public void setLabelArming(char mode, String label) {
         Panel.this.setLabelArming(mode, label);
      }

      public void alert(String message) {
         AlertNotifier.getDefault().fire(Panel.this, message);
      }
   }
}
