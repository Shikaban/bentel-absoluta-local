package plugin.absoluta.connection;

import cms.device.spi.AlertCallback;
import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.NewValue;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.openide.util.NbBundle;

class AlertListener implements MessageListener {
   private final AlertCallback alertCallback;
   private final PanelStatus panelStatus;

   public AlertListener(AlertCallback var1, PanelStatus var2) {
      this.alertCallback = var1;
      this.panelStatus = var2;
   }

   public void newValue(NewValue var1) {
   }

   public void error(DscError err) {
      if (err.getResponseCode() != null) {
         System.out.println("DEBUG: error received: " + err);
         Integer var2;
         if (err.isFor(Message.ARM)) {
            if (((Pair)err.getParam(Message.ARM)).getValue0() == null) {
               this.alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.arm.global"));
            } else {
               var2 = (Integer)((Pair)err.getParam(Message.ARM)).getValue0();
               String var3 = this.panelStatus.getPartitionLabel(var2);
               this.alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.arm.partition", var3));
            }
         } else {
            String var4;
            if (err.isFor(Message.SINGLE_ZONE_BYPASS_WRITE)) {
               var2 = (Integer)((Triplet)err.getParam(Message.SINGLE_ZONE_BYPASS_WRITE)).getValue1();
               Boolean var5 = (Boolean)((Triplet)err.getParam(Message.SINGLE_ZONE_BYPASS_WRITE)).getValue2();
               var4 = this.panelStatus.getZoneLabel(var2);
               if (var5) {
                  this.alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.bypass.zone", var4));
               } else {
                  this.alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.unbypass.zone", var4));
               }
            } else if (err.isFor(Message.SET_OUTPUT)) {
               var2 = (Integer)((Triplet)err.getParam(Message.SET_OUTPUT)).getValue1();
               Integer var6 = (Integer)((Triplet)err.getParam(Message.SET_OUTPUT)).getValue2();
               var4 = this.panelStatus.getOutputLabel(var2);
               if (var6 == 1) {
                  this.alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.output.close", var4));
               } else if (var6 == 2) {
                  this.alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.output.open", var4));
               }
            }
         }

      }
   }
}
