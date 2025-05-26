import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import cms.device.api.Device;
import cms.device.api.Input;
import cms.device.api.Output;
import cms.device.api.Panel;
import cms.device.api.Partition;
import cms.device.api.Input.Status;
import cms.device.api.Panel.Arming;
import cms.device.spi.PanelProvider;

class Callback implements PanelProvider.PanelCallback, MqttCallback {
   private MqttClient mqttClient;
   private String[] sensorIDs;
   private String[] partitionIDs;
   private String[] sensorNames;
   private String[] partitionNames;
   private String[] sensorStatuses;
   private String[] partitionStatuses;
   private String[] sensorTopics;
   private String[] partitionTopics;
   private Panel panel;
   private MqttConnectOptions connOpts;
   private int reconnectionAttempts = 0;
   private boolean isConnected = false;
   private static final int RECON_DELAY = 90;
   private static final boolean VERBOSE_DEBUG = false;

   public Callback(MqttClient mqttClient, Panel panel, MqttConnectOptions mqttOption) {
      this.mqttClient = mqttClient;
      this.panel = panel;
      this.connOpts = mqttOption;
   }

   public void connectionLost(Throwable var1) {
      System.out.println("WARN: Connessione persa con il broker MQTT: " + var1.getMessage() + ". Riconnessione...");
      this.reconnectWithDelay("broker MQTT");
   }

   public void connectionLost() {
      System.out.println("WARN: Connessione persa con la centrale! Riconnessione...");
      this.reconnectWithDelay("centrale");
   }

   public void alert(String var1) {
   }

   public void changeInputs(List<String> var1) {
      this.sensorIDs = (String[])var1.toArray(new String[0]);
      this.sensorNames = new String[Integer.parseInt(this.sensorIDs[this.sensorIDs.length - 1]) + 1];
      this.sensorTopics = new String[Integer.parseInt(this.sensorIDs[this.sensorIDs.length - 1]) + 1];
      this.sensorStatuses = new String[Integer.parseInt(this.sensorIDs[this.sensorIDs.length - 1]) + 1];
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: Sensore ID: " + String.valueOf(var1));
      }
   }

   public void changeOutputs(List<String> var1) {
   }

   public void changePartitions(List<String> msg) {
      this.partitionIDs = (String[])msg.toArray(new String[0]);
      this.partitionNames = new String[Integer.parseInt(this.partitionIDs[this.partitionIDs.length - 1]) + 1];
      this.partitionTopics = new String[Integer.parseInt(this.partitionIDs[this.partitionIDs.length - 1]) + 1];
      this.partitionStatuses = new String[Integer.parseInt(this.partitionIDs[this.partitionIDs.length - 1]) + 1];
      this.partitionNames[0] = "Globale";
      this.partitionTopics[0] = "ABS/global";

      if (!this.isConnected){
         this.isConnected = true;
         try {
               MqttMessage mqttMessage = new MqttMessage("Status: Connesso".getBytes());
               mqttMessage.setQos(1);
               this.mqttClient.publish("ABS/conn", mqttMessage);
         } catch (Exception ex) {
            System.out.println("ERROR: invio messaggio: " + "ABS/conn");
         }
      }

      try {
         this.mqttClient.subscribe(this.partitionTopics[0] + "/set");
      } catch (Exception ex) {
         System.out.println("ERROR: subscribe to: " + "ABS/global");
      }
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: Partizione ID: " + String.valueOf(msg));
      }
   }

   public void setArming(Panel.Arming actArming) {
      if (actArming == Arming.GLOBALLY_DISARMED) {
         this.partitionStatuses[0] = "Disarmato";
      } else if (actArming == Arming.GLOBALLY_ARMED) {
         this.partitionStatuses[0] = "Armato";
      } else if (actArming == Arming.PARTIALLY_ARMED) {
         this.partitionStatuses[0] = "Parziale";
      }

      try {
         String str = "Name: " + this.partitionNames[0] + " Status: " + this.partitionStatuses[0];
         MqttMessage msg = new MqttMessage(str.getBytes());
         msg.setQos(1);
         this.mqttClient.publish(this.partitionTopics[0], msg);
      } catch (Exception ex) {
         System.out.println("ERROR: invio messaggio: " + this.partitionTopics[0]);
      }
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: Stato generale allarme cambiato in: " + actArming.toString());
      }
   }

   public void setInputRemoteName(String sensorID, String sensorName) {
      int sensorIDInt = Integer.parseInt(sensorID);
      this.sensorNames[sensorIDInt] = sensorName;
      this.sensorTopics[sensorIDInt] = "ABS/sensor/" + sensorIDInt;
      try {
         String str = "Name: " + this.sensorNames[sensorIDInt] + " Status: " + this.sensorStatuses[sensorIDInt];
         MqttMessage msg = new MqttMessage(str.getBytes());
         msg.setQos(1);
         this.mqttClient.publish(this.sensorTopics[sensorIDInt], msg);
      } catch (Exception ex) {
         System.out.println("ERROR: invio messaggio: " + this.sensorTopics[sensorIDInt]);
      }
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: Sensore ID: " + sensorID + ", nome: " + sensorName);
      }
   }

   public void setInputStatus(String sensorID, Input.Status sensorStatus) {
      int sensorIDInt = Integer.parseInt(sensorID);
      if (sensorStatus != Status.ACTIVE && sensorStatus != Status.ALARM) {
         this.sensorStatuses[sensorIDInt] = "Off";
      } else {
         this.sensorStatuses[sensorIDInt] = "On";
      }

      if (this.sensorNames[sensorIDInt] != null) {
         try {
            String str = "Name: " + this.sensorNames[sensorIDInt] + " Status: " + this.sensorStatuses[sensorIDInt];
            MqttMessage msg = new MqttMessage(str.getBytes());
            msg.setQos(1);
            this.mqttClient.publish(this.sensorTopics[sensorIDInt], msg);
         } catch (Exception ex) {
            System.out.println("ERROR: invio messaggio: " + this.sensorTopics[sensorIDInt]);
         }
      }
      if(VERBOSE_DEBUG) {
         System.out.println("Sensore ID: " + sensorID + " stato cambiato in: " + sensorStatus.toString());
      }
   }

   public void setLabelArming(char var1, String var2) {
   }

   public void setOutputRemoteName(String var1, String var2) {
   }

   public void setOutputStatus(String var1, Output.Status var2) {
   }

   public void setPartitionArming(String partitionID, Partition.Arming partitionStatus) {
      int partitionIDInt = Integer.parseInt(partitionID);
      if (partitionStatus == cms.device.api.Partition.Arming.DISARMED) {
         this.partitionStatuses[partitionIDInt] = "Disarmata";
      } else if (partitionStatus == cms.device.api.Partition.Arming.AWAY) {
         this.partitionStatuses[partitionIDInt] = "Armata";
      } else if (partitionStatus == cms.device.api.Partition.Arming.STAY) {
         this.partitionStatuses[partitionIDInt] = "Armata home";
      } else if (partitionStatus == cms.device.api.Partition.Arming.NODELAY) {
         this.partitionStatuses[partitionIDInt] = "Armata notte";
      }

      if (this.partitionNames[partitionIDInt] != null) {
         try {
            String str = "Name: " + this.partitionNames[partitionIDInt] + " Status: " + this.partitionStatuses[partitionIDInt];
            MqttMessage msg = new MqttMessage(str.getBytes());
            msg.setQos(1);
            this.mqttClient.publish(this.partitionTopics[partitionIDInt], msg);
         } catch (Exception ex) {
            System.out.println("ERROR: invio messaggio: " + this.partitionTopics[partitionIDInt]);
         }
      }
      if(VERBOSE_DEBUG) {
         System.out.println("Partizione ID: " + partitionID + " stato: " + partitionStatus.toString());
      }
   }

   public void setPartitionRemoteName(String partitionID, String partitionName) {
      int partitionIDInt = Integer.parseInt(partitionID);
      this.partitionNames[partitionIDInt] = partitionName;
      this.partitionTopics[partitionIDInt] = "ABS/partition/" + (partitionIDInt);

      try{
         this.mqttClient.subscribe(this.partitionTopics[partitionIDInt] + "/set");
      } catch (Exception ex) {
         System.out.println("ERROR: subscribe to: " + this.partitionTopics[partitionIDInt]);
      }

      try {
         String str = "Name: " + this.partitionNames[partitionIDInt] + " Status: " + this.partitionStatuses[partitionIDInt];
         MqttMessage msg = new MqttMessage(str.getBytes());
         msg.setQos(1);
         this.mqttClient.publish(this.partitionTopics[partitionIDInt], msg);
      } catch (Exception ex) {
         System.out.println("ERROR: invio messaggio: " + this.partitionTopics[partitionIDInt]);
      }
      if(VERBOSE_DEBUG) {
         System.out.println("Partizione ID: " + partitionID + ", nome: " + partitionName);
      }
   }

   public void setPartitionStatus(String var1, Partition.Status var2) {
   }

   public void setPartitionsArming(Partition.Arming var1) {
   }

   public void setRemoteName(String var1) {
   }

   public void setStatus(Panel.Status var1) {
   }

   public void tagInputIntoPartition(String var1, List<String> var2) {
   }

   public void messageArrived(String topic, MqttMessage msg) {
      int idArray = ArrayUtils.indexOf(this.partitionTopics, topic.replace("/set", ""));
      if (idArray > 0 && idArray <= this.partitionIDs.length) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: Comando ricevuto per partizione numero: " + idArray + " nuovo stato: " + msg.toString());
         }
         switch (msg.toString()) {
            case "DISARM":
               this.panel.partitionArming(this.partitionIDs[idArray], cms.device.api.Partition.Arming.DISARMED);
               return;
            case "ARM_HOME":
               this.panel.partitionArming(this.partitionIDs[idArray], cms.device.api.Partition.Arming.STAY);
               return;
            case "ARM_AWAY":
               this.panel.partitionArming(this.partitionIDs[idArray], cms.device.api.Partition.Arming.AWAY);
               return;
            case "ARM_NIGHT":
               this.panel.partitionArming(this.partitionIDs[idArray], cms.device.api.Partition.Arming.NODELAY);
               return;
            default:
               System.out.println("WARN: Comando " + msg.toString() + " non valido");
         }
      } else if (idArray == 0) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: Comando ricevuto per stato globale: " + msg.toString());
         }
         switch (msg.toString()) {
            case "DISARM":
               this.panel.arming(Arming.GLOBALLY_DISARMED);
               return;
            case "ARM_AWAY":
               this.panel.arming(Arming.GLOBALLY_ARMED);
               return;
            case "MODE_A" :
               this.panel.modalityArming('A');
               return;
            case "MODE_B" :
               this.panel.modalityArming('B');
               return;
            case "MODE_C" :
               this.panel.modalityArming('C');
               return;
            case "MODE_D" :
               this.panel.modalityArming('D');
               return;
            default:
               System.out.println("WARN: Comando " + msg.toString() + " non valido");
         }
      } else {
         System.out.println("WARN: ID " + idArray + " non valido");
      }
   }

   public void deliveryComplete(IMqttDeliveryToken var1) {
   }

   private void reconnectWithDelay(String objName) {
      this.isConnected = false;
      ++this.reconnectionAttempts;
      System.out.println("WARN: Tentativo di riconnessione " + this.reconnectionAttempts + " a " + objName + " in " + RECON_DELAY + " secondi...");
      try {
         MqttMessage msg = new MqttMessage("Status: Scollegato".getBytes());
         msg.setQos(1);
         this.mqttClient.publish("ABS/conn", msg);
      } catch (Exception ex) {
         System.out.println("ERROR: invio messaggio: " + "ABS/conn");
      }
      try {
         TimeUnit.SECONDS.sleep((long)RECON_DELAY);
         if (objName.equals("centrale")) {
            Device.Status var2 = this.panel.connect();
            if (var2 == cms.device.api.Device.Status.UNREACHABLE) {
               if(VERBOSE_DEBUG) {
                  System.out.println("DEBUG: Rilevato 'busy panel'. Tentativo di riconnessione forzata...");
               }
               throw new Exception("Busy panel");
            }
         } else if (objName.equals("broker MQTT")) {
            this.mqttClient.connect(this.connOpts);
         }

         this.reconnectionAttempts = 0;
         this.isConnected = true;
         try {
            MqttMessage msg = new MqttMessage("Status: Connesso".getBytes());
            msg.setQos(1);
            this.mqttClient.publish("ABS/conn", msg);
         } catch (Exception ex) {
            System.out.println("ERROR: invio messaggio: " + "ABS/conn");
         }
      } catch (InterruptedException ex) {
         Thread.currentThread().interrupt();
         this.handleReconnectionFailure(objName, ex);
      } catch (Exception ex) {
         this.handleReconnectionFailure(objName, ex);
      }
   }

   private void handleReconnectionFailure(String name, Exception ex) {
      System.err.println("ERROR: Impossibile riconnettersi a " + name + " Causa: " + ex.getMessage());
      ex.printStackTrace();
      this.reconnectWithDelay(name);
   }
}