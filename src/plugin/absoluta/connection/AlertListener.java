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

   public AlertListener(AlertCallback alertCallback, PanelStatus panelStatus) {
      this.alertCallback = alertCallback;
      this.panelStatus = panelStatus;
   }

   public void newValue(NewValue newValue) {
   }

   public void error(DscError error) {
      if (error.getResponseCode() == null) {
         return;
      }

      System.out.println("DEBUG: error received: " + error);

      if (error.isFor(Message.ARM)) {
         // ARM error: check if it's global or for a specific partition
         Pair<Integer, ?> armParam = (Pair<Integer, ?>) error.getParam(Message.ARM);
         Integer partitionId = armParam.getValue0();
         if (partitionId == null) {
               alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.arm.global"));
         } else {
               String partitionLabel = panelStatus.getPartitionLabel(partitionId);
               alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.arm.partition", partitionLabel));
         }
      } else if (error.isFor(Message.SINGLE_ZONE_BYPASS_WRITE)) {
         // Zone bypass/unbypass error
         Triplet<?, Integer, Boolean> bypassParam = (Triplet<?, Integer, Boolean>) error.getParam(Message.SINGLE_ZONE_BYPASS_WRITE);
         Integer zoneId = bypassParam.getValue1();
         Boolean isBypass = bypassParam.getValue2();
         String zoneLabel = panelStatus.getZoneLabel(zoneId);

         if (isBypass) {
               alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.bypass.zone", zoneLabel));
         } else {
               alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.unbypass.zone", zoneLabel));
         }

         // Output set error (if present)
         Triplet<?, Integer, Integer> outputParam = (Triplet<?, Integer, Integer>) error.getParam(Message.SET_OUTPUT);
         Integer outputId = outputParam.getValue1();
         Integer outputState = outputParam.getValue2();
         String outputLabel = panelStatus.getOutputLabel(outputId);

         if (outputState == 1) {
               alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.output.close", outputLabel));
         } else if (outputState == 2) {
               alertCallback.alert(NbBundle.getMessage(AlertListener.class, "Alert.output.open", outputLabel));
         }
      }
   }
}
