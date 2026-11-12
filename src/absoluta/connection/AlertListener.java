package absoluta.connection;

import protocol.dsc.DscError;
import protocol.dsc.Message;
import protocol.dsc.MessageListener;
import protocol.dsc.NewValue;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import absoluta.spi.AlertCallback;

import java.util.logging.Logger;

class AlertListener implements MessageListener {
   private static final Logger logger = Logger.getLogger(AlertListener.class.getName());
   private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("absoluta.connection.Bundle");
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

		logger.fine("Error received: " + error);

		if (error.isFor(Message.ARM)) {
         // ARM error: check if it's global or for a specific partition
         Pair<Integer, ?> armParam = (Pair<Integer, ?>) error.getParam(Message.ARM);
         Integer partitionId = armParam.getValue0();
         if (partitionId == null) {
            List<Integer> targets = panelStatus.getPendingArmPartitions();
            List<String> failedLabels = new ArrayList<>();
            if (targets != null) {
               for (Integer id : targets) {
                  if (!Boolean.TRUE.equals(panelStatus.getPartitionReady(id))) {
                     String label = panelStatus.getPartitionLabel(id);
                     failedLabels.add(label != null ? label : "partition " + id);
                  }
               }
            }
            if (failedLabels.isEmpty()) {
               alertCallback.alert(BUNDLE.getString("Alert.arm.global"));
            } else {
               alertCallback.alert(MessageFormat.format(BUNDLE.getString("Alert.arm.global.partitions"), String.join(", ", failedLabels)));
            }
            revertOptimisticArming(null);
         } else {
            String partitionLabel = panelStatus.getPartitionLabel(partitionId);
            alertCallback.alert(MessageFormat.format(BUNDLE.getString("Alert.arm.partition"), partitionLabel));
            revertOptimisticArming(partitionId);
         }
		} else if (error.isFor(Message.SINGLE_ZONE_BYPASS_WRITE)) {
         // Zone bypass/unbypass error
         Triplet<?, Integer, Boolean> bypassParam = (Triplet<?, Integer, Boolean>) error.getParam(Message.SINGLE_ZONE_BYPASS_WRITE);
         Integer zoneId = bypassParam.getValue1();
         Boolean isBypass = bypassParam.getValue2();
         String zoneLabel = panelStatus.getZoneLabel(zoneId);

         if (isBypass) {
            alertCallback.alert(MessageFormat.format(BUNDLE.getString("Alert.bypass.zone"), zoneLabel));
         } else {
            alertCallback.alert(MessageFormat.format(BUNDLE.getString("Alert.unbypass.zone"), zoneLabel));
         }

         // Output set error (if present)
         Triplet<?, Integer, Integer> outputParam = (Triplet<?, Integer, Integer>) error.getParam(Message.SET_OUTPUT);
         Integer outputId = outputParam.getValue1();
         Integer outputState = outputParam.getValue2();
         String outputLabel = panelStatus.getOutputLabel(outputId);

         if (outputState == 1) {
            alertCallback.alert(MessageFormat.format(BUNDLE.getString("Alert.output.close"), outputLabel));
         } else if (outputState == 2) {
            alertCallback.alert(MessageFormat.format(BUNDLE.getString("Alert.output.open"), outputLabel));
         }
		}
	}

	/**
	 * Clears the optimistic ARMING state set by {@link Commander} when the panel refuses an arm
	 * request (e.g. an open zone). Without this, the ARMING-preserving latch in
	 * {@link StatusListener} keeps re-applying the optimistic state on every status poll, leaving
	 * the Home Assistant entity stuck in "arming" indefinitely.
	 *
	 * <p>For a partition-specific arm the partition is reverted to DISARMED. The global state is
	 * always reset to GLOBALLY_DISARMED to release the global latch; the next status poll recomputes
	 * it from the real partition states (so genuinely armed partitions are restored within one cycle).
	 */
	private void revertOptimisticArming(Integer partitionId) {
		if (partitionId != null) {
			panelStatus.updatePartitionArming(partitionId, PanelStatus.PartitionArming.DISARMED);
		}
		panelStatus.updateGlobalArming(PanelStatus.GlobalArming.GLOBALLY_DISARMED);
	}
}
