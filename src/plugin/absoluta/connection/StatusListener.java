
package plugin.absoluta.connection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

import cms.device.api.Output.Status;
import cms.device.api.Partition.Arming;
import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.NewValue;

import java.util.Iterator;
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

   public void newValue(NewValue var1) {
      List var2;
      if (var1.isFor(Message.PARTITION_ASSIGNMENT_CONFIGURATION)) {
         var2 = (List)var1.getValue(Message.PARTITION_ASSIGNMENT_CONFIGURATION);
         this.panelStatus.setPartitions(ImmutableList.copyOf(var2));
      } else {
         List var3;
         if (var1.isFor(Message.PARTITION_ZONES)) {
            Integer var6 = (Integer)var1.getParam(Message.PARTITION_ZONES);
            if (var6 == null) {
               var3 = (List)var1.getValue(Message.PARTITION_ZONES);
               this.panelStatus.setZones(ImmutableList.copyOf(var3));
            } else {
               System.out.println("WARN: unexpected partition number for partition zones: " + var6);
            }
         } else if (var1.isFor(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)) {
            var2 = (List)((Pair)var1.getValue(Message.ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS)).getValue0();
            this.panelStatus.setOutputs(ImmutableList.copyOf(var2));
         } else if (var1.isFor(Message.PARTITION_STATUSES)) {
            var2 = (List)var1.getParam(Message.PARTITION_STATUSES);
            var3 = (List)var1.getValue(Message.PARTITION_STATUSES);

            assert var2.size() == var3.size();

            for(int var4 = 0; var4 < var2.size(); ++var4) {
               int var5 = (Integer)var2.get(var4);
               this.updatePartitionStatus(var5, (List)var3.get(var4));
            }
         } else {
            int var7;
            if (var1.isFor(Message.ZONE_STATUSES)) {
               var7 = (Integer)((Pair)var1.getParam(Message.ZONE_STATUSES)).getValue0();
               var3 = (List)var1.getValue(Message.ZONE_STATUSES);

               for(Iterator var11 = var3.iterator(); var11.hasNext(); ++var7) {
                  List<Boolean> var13 = (List)var11.next();
                  this.updateZoneStatus(var7, var13);
               }
            } else if (var1.isFor(Message.ABSOLUTA_COMMAND_OUTPUT_ACTIVATION)) {
               var2 = (List)var1.getValue(Message.ABSOLUTA_COMMAND_OUTPUT_ACTIVATION);
               UnmodifiableIterator var8 = this.panelStatus.getOutputs().iterator();

               while(var8.hasNext()) {
                  Integer var12 = (Integer)var8.next();
                  Status var14 = var2.contains(var12) ? Status.CLOSED : Status.OPEN;
                  this.panelStatus.setOutputStatus(var12, var14);
               }
            } else if (var1.isFor(Message.ABSOLUTA_SYSTEM_LABEL)) {
               String var9 = ((String)var1.getValue(Message.ABSOLUTA_SYSTEM_LABEL)).trim();
               this.panelStatus.setSystemLabel(var9);
            } else {
               String var10;
               if (var1.isFor(Message.ABSOLUTA_PARTITION_LABEL)) {
                  var7 = (Integer)var1.getParam(Message.ABSOLUTA_PARTITION_LABEL);
                  var10 = ((String)var1.getValue(Message.ABSOLUTA_PARTITION_LABEL)).trim();
                  this.panelStatus.setPartitionLabel(var7, var10);
               } else if (var1.isFor(Message.ABSOLUTA_ZONE_LABEL)) {
                  var7 = (Integer)var1.getParam(Message.ABSOLUTA_ZONE_LABEL);
                  var10 = ((String)var1.getValue(Message.ABSOLUTA_ZONE_LABEL)).trim();
                  this.panelStatus.setZoneLabel(var7, var10);
               } else if (var1.isFor(Message.ABSOLUTA_OUTPUT_LABEL)) {
                  var7 = (Integer)var1.getParam(Message.ABSOLUTA_OUTPUT_LABEL);
                  var10 = ((String)var1.getValue(Message.ABSOLUTA_OUTPUT_LABEL)).trim();
                  this.panelStatus.setOutputLabel(var7, var10);
               } else if (var1.isFor(Message.ABSOLUTA_ARMING_MODE_LABEL)) {
                  var7 = (Integer)var1.getParam(Message.ABSOLUTA_ARMING_MODE_LABEL);
                  var10 = ((String)var1.getValue(Message.ABSOLUTA_ARMING_MODE_LABEL)).trim();
                  this.panelStatus.setArmingModeLabel(var7, var10);
               }
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

      this.panelStatus.setPartitionArming(selectedPartitionID, newPartitionmode);
      boolean systemArmed = false;
      boolean onePartitionDisarmed = false;
      boolean noValidData = false;
      UnmodifiableIterator listPartition = this.panelStatus.getPartitions().iterator();

      while(listPartition.hasNext()) {
         int partitionID = (Integer)listPartition.next();
         Arming partitionMode = this.panelStatus.getPartitionArming(partitionID);
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
   }

   private void updateZoneStatus(int var1, List<Boolean> var2) {
      cms.device.api.Input.Status var3;
      if ((Boolean)var2.get(ZI_BYPASSED)) {
         var3 = cms.device.api.Input.Status.BYPASSED;
      } else if (!(Boolean)var2.get(ZI_TAMPER) && !(Boolean)var2.get(ZI_DELINQUENCY)) {
         if (!(Boolean)var2.get(ZI_FAULT) && !(Boolean)var2.get(ZI_LOW_BATTERY)) {
            if (!(Boolean)var2.get(ZI_ALARM) && !(Boolean)var2.get(ZI_ALARM_IN_MEMORY)) {
               if ((Boolean)var2.get(ZI_OPEN)) {
                  var3 = cms.device.api.Input.Status.ACTIVE;
               } else {
                  var3 = cms.device.api.Input.Status.OK;
               }
            } else {
               var3 = cms.device.api.Input.Status.ALARM;
            }
         } else {
            var3 = cms.device.api.Input.Status.FAULT;
         }
      } else {
         var3 = cms.device.api.Input.Status.TAMPER;
      }

      this.panelStatus.setZoneBypass(var1, var3 == cms.device.api.Input.Status.BYPASSED);
      this.panelStatus.setZoneStatus(var1, var3);
   }

}
