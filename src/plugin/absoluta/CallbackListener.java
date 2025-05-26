package plugin.absoluta;

import cms.device.spi.PanelProvider.PanelCallback;
import plugin.absoluta.connection.CustomizedArmingModes;
import plugin.absoluta.connection.PanelStatus;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

class CallbackListener implements PropertyChangeListener {
   private final PanelCallback callback;
   private final PanelStatus panelStatus;

   CallbackListener(PanelCallback var1, PanelStatus var2) {
      this.callback = (PanelCallback)Objects.requireNonNull(var1);
      this.panelStatus = (PanelStatus)Objects.requireNonNull(var2);
   }

   public void propertyChange(PropertyChangeEvent var1) {
      assert this.panelStatus == var1.getSource();

      if (var1.getNewValue() != null) {
         if (var1 instanceof IndexedPropertyChangeEvent) {
            int var2 = ((IndexedPropertyChangeEvent)var1).getIndex();
            String var3 = String.valueOf(var2);
            String var4 = var1.getPropertyName();
            byte var5 = -1;
            switch(var4.hashCode()) {
            case -856264170:
               if (var4.equals("OUTPUT_LABEL")) {
                  var5 = 6;
               }
               break;
            case -556449552:
               if (var4.equals("OUTPUT_STATUS")) {
                  var5 = 5;
               }
               break;
            case -173791471:
               if (var4.equals("ARMING_MODE_LABEL")) {
                  var5 = 7;
               }
               break;
            case 302271775:
               if (var4.equals("PARTITION_LABEL")) {
                  var5 = 2;
               }
               break;
            case 460090341:
               if (var4.equals("ZONE_STATUS")) {
                  var5 = 3;
               }
               break;
            case 481601307:
               if (var4.equals("PARTITION_ARMING")) {
                  var5 = 0;
               }
               break;
            case 839095425:
               if (var4.equals("ZONE_LABEL")) {
                  var5 = 4;
               }
               break;
            case 998426375:
               if (var4.equals("PARTITION_STATUS")) {
                  var5 = 1;
               }
            }

            switch(var5) {
            case 0:
               this.callback.setPartitionArming(var3, this.panelStatus.getPartitionArming(var2));
               break;
            case 1:
               this.callback.setPartitionStatus(var3, this.panelStatus.getPartitionStatus(var2));
               break;
            case 2:
               this.callback.setPartitionRemoteName(var3, this.panelStatus.getPartitionLabel(var2));
               break;
            case 3:
               this.callback.setInputStatus(var3, this.panelStatus.getZoneStatus(var2));
               break;
            case 4:
               this.callback.setInputRemoteName(var3, this.panelStatus.getZoneLabel(var2));
               break;
            case 5:
               this.callback.setOutputStatus(var3, this.panelStatus.getOutputStatus(var2));
               break;
            case 6:
               this.callback.setOutputRemoteName(var3, this.panelStatus.getOutputLabel(var2));
               break;
            case 7:
               Character var6 = (Character)CustomizedArmingModes.ARMING_MODE_LABELS.get(var2);
               if (var6 != null) {
                  this.callback.setLabelArming(var6, this.panelStatus.getArmingModeLabel(var2));
               }
            }
         } else {
            String var8 = var1.getPropertyName();
            byte var9 = -1;
            switch(var8.hashCode()) {
            case -1824685732:
               if (var8.equals("TROUBLES")) {
                  var9 = 6;
               }
               break;
            case -1413893948:
               if (var8.equals("SYSTEM_LABEL")) {
                  var9 = 2;
               }
               break;
            case -388131982:
               if (var8.equals("OUTPUTS")) {
                  var9 = 5;
               }
               break;
            case -304980958:
               if (var8.equals("GLOBAL_ARMING")) {
                  var9 = 1;
               }
               break;
            case 85547559:
               if (var8.equals("ZONES")) {
                  var9 = 4;
               }
               break;
            case 525547209:
               if (var8.equals("PARTITIONS")) {
                  var9 = 3;
               }
               break;
            case 1463550067:
               if (var8.equals("CONNECTION_STATUS")) {
                  var9 = 0;
               }
            }

            switch(var9) {
            case 0:
               if (PanelStatus.ConnectionStatus.CONNECTED == var1.getOldValue() && PanelStatus.ConnectionStatus.DISCONNECTED == var1.getNewValue()) {
                  this.callback.connectionLost();
               }
               break;
            case 1:
               this.callback.setArming(this.panelStatus.getGlobalArming());
               break;
            case 2:
               this.callback.setRemoteName(this.panelStatus.getSystemLabel());
               break;
            case 3:
               this.callback.changePartitions(toStringList(this.panelStatus.getPartitions()));
               break;
            case 4:
               List<String> var10 = toStringList(this.panelStatus.getZones());
               this.callback.changeInputs(var10);
               List<String> var13 = toStringList(this.panelStatus.getPartitions());
               Iterator var12 = var13.iterator();

               while(var12.hasNext()) {
                  String var7 = (String)var12.next();
                  this.callback.tagInputIntoPartition(var7, var10);
               }

               return;
            case 5:
               this.callback.changeOutputs(toStringList(this.panelStatus.getOutputs()));
               break;
            }
         }

      }
   }

   private static List<String> toStringList(List<Integer> var0) {
      List<String> var1 = new ArrayList(var0.size());
      Iterator var2 = var0.iterator();

      while(var2.hasNext()) {
         Integer var3 = (Integer)var2.next();
         var1.add(var3.toString());
      }

      return var1;
   }
}
