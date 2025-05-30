package cms.device.api;

import java.util.List;

import cms.device.spi.DeviceProvider;
import cms.device.spi.PanelProvider;

final class Callbacks {
   static enum DeviceCb implements DeviceProvider.DeviceCallback {
      DUMMY;

      public void connectionLost() {
      }

      public void setRemoteName(String var1) {
      }
   }

   static enum PanelCb implements PanelProvider.PanelCallback {
      DUMMY;

      public void changePartitions(List<String> var1) {
      }

      public void setPartitionsArming(Partition.Arming var1) {
      }

      public void setPartitionArming(String var1, Partition.Arming var2) {
      }

      public void changeInputs(List<String> var1) {
      }

      public void tagInputIntoPartition(String var1, List<String> var2) {
      }

      public void setArming(Panel.Arming var1) {
      }

      public void setStatus(Panel.Status var1) {
      }

      public void setInputStatus(String var1, Input.Status var2) {
      }

      public void setPartitionStatus(String var1, Partition.Status var2) {
      }

      public void connectionLost() {
      }

      public void setLabelArming(char var1, String var2) {
      }

      public void alert(String var1) {
      }

      public void setPartitionRemoteName(String var1, String var2) {
      }

      public void setInputRemoteName(String var1, String var2) {
      }

      public void changeOutputs(List<String> var1) {
      }

      public void setOutputRemoteName(String var1, String var2) {
      }

      public void setOutputStatus(String var1, Output.Status var2) {
      }
   }
}