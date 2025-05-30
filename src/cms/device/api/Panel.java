package cms.device.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import cms.device.spi.PanelProvider;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public final class Panel implements Provider, DeviceOrPanel {
   private final InstanceContent content = new InstanceContent();
   private final Lookup lookup;
   private boolean connected;
   private boolean discovered;
   private Panel.Arming arming;
   public Panel.Status status;
   private boolean alarmed;
   private final PanelProvider impl;
   private final ChangeSupport changeSupport;
   private final Map<Character, String> labelArming;
   private final Map<String, Partition> partitions;
   private final Map<String, Input> inputs;
   final OutputSupport outputSupport;

   public Panel(PanelProvider provider) {
      this.lookup = new AbstractLookup(this.content);
      this.arming = Panel.Arming.GLOBALLY_DISARMED;
      this.status = Panel.Status.OK;

      this.connected = false;
      this.alarmed = false;
      this.changeSupport = new ChangeSupport(this);
      this.labelArming = new LinkedHashMap();
      this.partitions = new LinkedHashMap();
      this.inputs = new LinkedHashMap();
      this.outputSupport = new OutputSupport(this, this::doOutputAction);
      this.impl = provider;
      provider.initialize(new Panel.Callback());
   }

   public Lookup getLookup() {
      return this.lookup;
   }

   public Device.Status connect() {
      if (this.connected) {
         return Device.Status.SUCCESS;
      } else {
         Device.Status var1 = this.impl.connect();
         if (var1 != Device.Status.SUCCESS) {
            return var1;
         } else {
            this.connected = true;
            this.discovered = true;

            this.fireChange();
            return Device.Status.SUCCESS;
         }
      }
   }

   public void disconnect() {
      if (this.connected) {
         this.impl.disconnect();
         this.connected = false;

         this.fireChange();
      }

   }

   public boolean isConnected() {
      return this.connected;
   }

   public boolean isUserConnectable() {
      return true;
   }

   public boolean isDiscovered() {
      return this.discovered;
   }

   void setDiscovered(boolean var1) {
      this.discovered = var1;
   }

   public boolean isAlarmed() {
      return this.alarmed;
   }

   public void setAlarmed(boolean var1) {
      if (this.alarmed != var1) {
         this.alarmed = var1;
         this.changeSupport.fireChange();
      }

   }

   public void arming(Panel.Arming mode) {
      this.impl.arming(mode);
   }

   public Panel.Arming getArming() {
      return this.arming;
   }

   void setArming(Panel.Arming newMode) {
      if (this.arming != newMode) {
         this.arming = newMode;
         this.changeSupport.fireChange();
      }
   }

   public Panel.Status getStatus() {
      return this.status;
   }

   void setStatus(Panel.Status newStatus) {
      if (this.status != newStatus) {
         this.status = newStatus;
         this.changeSupport.fireChange();
      }
   }

   public Map<String, String> getSettings() {
      return this.impl.getSettings();
   }

   public void addChangeListener(ChangeListener var1) {
      this.changeSupport.addChangeListener(var1);
   }

   public void removeChangeListener(ChangeListener var1) {
      this.changeSupport.removeChangeListener(var1);
   }

   public void fireChange() {
      this.changeSupport.fireChange();
      Iterator var1 = this.getPartitions().values().iterator();

      while(var1.hasNext()) {
         Partition var2 = (Partition)var1.next();
         var2.fireChange();
      }

      var1 = this.getInputs().values().iterator();

      while(var1.hasNext()) {
         Input var3 = (Input)var1.next();
         var3.fireChange();
      }
   }

   void doChangePartitions(Iterable<String> var1) {
      ImmutableList var2 = ImmutableList.copyOf(this.partitions.keySet());
      if (!Iterables.elementsEqual(var2, var1)) {
         LinkedHashMap var3 = Maps.newLinkedHashMap();
         Iterator var4 = var1.iterator();

         while(var4.hasNext()) {
            String var5 = (String)var4.next();
            if (this.partitions.containsKey(var5)) {
               var3.put(var5, (Partition)this.partitions.remove(var5));
            } else {
               var3.put(var5, new Partition(this));
            }
         }

         this.partitions.clear();
         this.partitions.putAll(var3);
         this.fireChange();
      }

   }

   public Map<String, Partition> getPartitions() {
      return this.partitions;
   }

   public void partitionArming(String var1, Partition.Arming var2) {
      this.impl.partitionArming(var1, var2);
   }

   private void doChangeInputs(Iterable<String> var1) {
      ImmutableList var2 = ImmutableList.copyOf(this.inputs.keySet());
      if (!Iterables.elementsEqual(var2, var1)) {
         LinkedHashMap var3 = Maps.newLinkedHashMap();
         Iterator var4 = var1.iterator();

         while(var4.hasNext()) {
            String var5 = (String)var4.next();
            if (this.inputs.containsKey(var5)) {
               var3.put(var5, (Input)this.inputs.remove(var5));
            } else {
               var3.put(var5, new Input(this));
            }
         }

         this.inputs.clear();
         this.inputs.putAll(var3);
         this.fireChange();
      }
   }

   public Map<String, Input> getInputs() {
      return this.inputs;
   }

   public void bypassInput(String zoneID, boolean setBypassed) {
      this.impl.setBypassed(zoneID, setBypassed);
   }

   private void doOutputAction(String var1, Output.Action var2) {
      this.impl.doOutputAction(var1, var2);
   }

   public boolean checkArmingSupport(char var1) {
      return this.impl.armingSupport(var1);
   }

   public void modalityArming(char mode) {
      this.impl.armingSet(mode);
   }

   public String getLabelArming(char var1) {
      return (String)this.labelArming.get(var1);
   }

   void setLabelArming(char var1, String var2) {
      this.labelArming.put(var1, var2);
   }

   public Map<String, Output> getOutputs() {
      return this.outputSupport.getOutputs();
   }

   public static enum Arming {
      GLOBALLY_ARMED,
      PARTIALLY_ARMED,
      GLOBALLY_DISARMED;
   }

   public static enum Status {
      TAMPER,
      FAULT,
      OK;
   }

   private class Callback implements PanelProvider.PanelCallback {

      public void setArming(Panel.Arming var1) {
         Panel.this.setArming(var1);
      }

      public void setStatus(Panel.Status var1) {
         Panel.this.setStatus(var1);
      }

      public void changePartitions(List<String> var1) {
         Panel.this.doChangePartitions(var1);
      }

      public void setPartitionRemoteName(String var1, String var2) {
         ((Partition)Panel.this.getPartitions().get(var1)).setRemoteName(var2);
      }

      public void setPartitionsArming(Partition.Arming var1) {
         Iterator var2 = Panel.this.getPartitions().values().iterator();

         while(var2.hasNext()) {
            Partition var3 = (Partition)var2.next();
            var3.setArming(var1);
         }

      }

      public void setPartitionArming(String var1, Partition.Arming var2) {
         ((Partition)Panel.this.getPartitions().get(var1)).setArming(var2);
      }

      public void setPartitionStatus(String var1, Partition.Status var2) {
         ((Partition)Panel.this.getPartitions().get(var1)).setStatus(var2);
      }

      public void changeInputs(List<String> var1) {
         Panel.this.doChangeInputs(var1);
      }

      public void setInputRemoteName(String var1, String var2) {
         ((Input)Panel.this.getInputs().get(var1)).setRemoteName(var2);
      }

      public void setInputStatus(String var1, Input.Status var2) {
         ((Input)Panel.this.getInputs().get(var1)).setStatus(var2);
      }

      public void tagInputIntoPartition(String var1, List<String> var2) {
         ((Partition)Panel.this.getPartitions().get(var1)).addInputs(var2);
      }

      public void changeOutputs(List<String> var1) {
         Panel.this.outputSupport.changeOutputs(var1);
      }

      public void setOutputRemoteName(String var1, String var2) {
         ((Output)Panel.this.getOutputs().get(var1)).setRemoteName(var2);
      }

      public void setOutputStatus(String var1, Output.Status var2) {
         ((Output)Panel.this.getOutputs().get(var1)).setStatus(var2);
      }

      public void connectionLost() {
         System.out.println("INFO: connection lost on: " + Panel.this);
         Panel.this.disconnect();
      }

      public void setLabelArming(char var1, String var2) {
         Panel.this.setLabelArming(var1, var2);
      }

      public void alert(String var1) {
         AlertNotifier.getDefault().fire(Panel.this, var1);
      }
   }
}
