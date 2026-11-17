package absoluta.spi;

import java.util.List;

import absoluta.AbsolutaPanelProvider.providerConnStatus;
import absoluta.AbsolutaPanelProvider.providerStatus;
import absoluta.connection.PanelStatus.GlobalArming;
import absoluta.connection.PanelStatus.InputStatus;
import absoluta.connection.PanelStatus.OutputAction;
import absoluta.connection.PanelStatus.OutputStatus;
import absoluta.connection.PanelStatus.PartitionArming;
import absoluta.connection.PanelStatus.PartitionReadiness;
import absoluta.connection.PanelStatus.PartitionStatus;

public interface PanelProvider {
   void initialize(PanelProvider.PanelCallback callback);

   providerConnStatus connect();

   void disconnect();

   void setGlobalArming(GlobalArming armingMode);

   void setPartitionArming(int partitionId, PartitionArming armingMode);

   void setZoneBypass(int zoneID, boolean setBypassed);

   void setOutput(int outputId, OutputAction action);

   void setModeArming(char mode);

   public interface PanelCallback extends AlertCallback {
      void connectionLost();

      void updateGlobalArming(GlobalArming arming);

      void setStatus(providerStatus status);

      void getAllPartitions(List<Integer> partitions);

      void updatePartitionName(int partitionId, String name);

      void updatePartitionArming(int partitionId, PartitionArming arming);

      void updatePartitionReadiness(int partitionId, PartitionReadiness readiness);

      void updatePartitionStatus(int partitionId, PartitionStatus status);

      void getAllZones(List<Integer> zones);

      void updateZoneName(int zoneId, String name);

      void updateZoneStatus(int zoneId, InputStatus status);

      void updateZoneBypass(int zoneId, boolean bypass);

      void tagZoneIntoPartition(int partitionId, List<Integer> zones);

      void getAllOutputs(List<Integer> outputs);

      void updateOutputName(int outputId, String name);

      void updateOutputStatus(int outputId, OutputStatus status);

      void updateModeLabel(char mode, String label);

      void updatePanelFaults(List<String> panelFaults);

      void updateAlarmMemory(List<String> partitionsWithAlarmMemory);
   }
}