package cms.device.spi;

import java.util.List;
import java.util.Map;

import cms.device.api.Input;
import cms.device.api.Output;
import cms.device.api.Panel;
import cms.device.api.Partition;

public interface PanelProvider {
   void initialize(PanelProvider.PanelCallback var1);

   Panel.connStatus connect();

   void disconnect();

   void arming(Panel.Arming var1);

   void partitionArming(String var1, Partition.Arming var2);

   void setBypassed(String var1, boolean var2);

   void doOutputAction(String var1, Output.Action var2);

   boolean armingSupport(char var1);

   void armingSet(char var1);

   Map<String, String> getSettings();

   public interface PanelCallback extends ConnectionListener, AlertCallback {

      void setArming(Panel.Arming var1);

      void setStatus(Panel.Status var1);

      void changePartitions(List<String> var1);

      void setPartitionRemoteName(String var1, String var2);

      void setPartitionsArming(Partition.Arming var1);

      void setPartitionArming(String var1, Partition.Arming var2);

      void setPartitionStatus(String var1, Partition.Status var2);

      void changeInputs(List<String> var1);

      void setInputRemoteName(String var1, String var2);

      void setInputStatus(String var1, Input.Status var2);

      void tagInputIntoPartition(String var1, List<String> var2);

      void changeOutputs(List<String> var1);

      void setOutputRemoteName(String var1, String var2);

      void setOutputStatus(String var1, Output.Status var2);

      void setLabelArming(char var1, String var2);
   }
}
