import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import cms.device.api.Input;
import cms.device.api.Output;
import cms.device.api.Panel;
import cms.device.api.Partition;
import cms.device.api.Input.Status;
import cms.device.api.Panel.Arming;
import cms.device.api.Panel.ConnStatus;
import cms.device.spi.PanelProvider;

class Callback implements PanelProvider.PanelCallback, MqttCallback {
   private MqttClient mqttClient;
   private int[] sensorIDs;
   private int[] partitionIDs;
   private String[] sensorNames;
   private String[] partitionNames;
   private String[] sensorStatuses;
   private String[] partitionArmStatuses;
   private String[] partitionStatuses;
   private String[] sensorTopics;
   private String[] partitionTopics;
   private String[] modeNames = new String[4];
   private Panel panel;
   private MqttConnectOptions connOpts;
   private int reconnectionAttempts = 0;
   private boolean isConnected = false;
   private static final int RECON_DELAY = 90;
   private static final boolean VERBOSE_DEBUG = false;
   private HashSet<Integer> sensorDiscoverySent = new HashSet<>();
   private HashSet<Integer> partitionDiscoverySent = new HashSet<>();
   private HashSet<Integer> modeDiscoverySent = new HashSet<>();
   private Boolean discoveryEnabled;

   public Callback(MqttClient mqttClient, Panel panel, MqttConnectOptions mqttOption, Boolean discoveryEnabled) {
      this.mqttClient = mqttClient;
      this.panel = panel;
      this.connOpts = mqttOption;
      this.discoveryEnabled = discoveryEnabled;
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

   public void changeInputs(List<String> msg) {
      this.sensorIDs = new int[msg.size()];
      int maxId = 0;
      for (int i = 0; i < msg.size(); i++) {
         try {
            this.sensorIDs[i] = Integer.parseInt(msg.get(i));
            if (this.sensorIDs[i] > maxId) maxId = this.sensorIDs[i];
         } catch (NumberFormatException e) {
            System.err.println("Errore di parsing sensorID: '" + msg.get(i) + "' non è un intero valido.");
            this.sensorIDs[i] = -1;
         }
      }
      this.sensorNames = new String[maxId + 1];
      this.sensorTopics = new String[maxId + 1];
      this.sensorStatuses = new String[maxId + 1];
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: Sensore ID: " + msg);
      }
   }

   public void changeOutputs(List<String> var1) {
   }

   public void changePartitions(List<String> msg) {
      this.partitionIDs = new int[msg.size() + 1];
      this.partitionIDs[0] = 0;
      for (int i = 0; i < msg.size(); i++) {
         try {
            this.partitionIDs[i + 1] = Integer.parseInt(msg.get(i));
         } catch (NumberFormatException e) {
            System.err.println("Errore di parsing partitionID: '" + msg.get(i) + "' non è un intero valido.");
            this.partitionIDs[i + 1] = -1;
         }
      }
      //TODO: #STEFANO valutare se unire globale alle altre partizioni
      this.partitionNames = new String[this.partitionIDs.length];
      this.partitionTopics = new String[this.partitionIDs.length];
      this.partitionArmStatuses = new String[this.partitionIDs.length];
      this.partitionStatuses = new String[this.partitionIDs.length];
      this.partitionNames[0] = "Globale";
      this.partitionTopics[0] = "ABS/global";

      // Invia discovery per la partizione globale (id 0)
      if (discoveryEnabled && !partitionDiscoverySent.contains(0)) {
         String topic = "homeassistant/alarm_control_panel/absoluta_partition_global/config";
         String payload = "{" +
            "\"name\": \"Globale\"," +
            "\"state_topic\": \"ABS/global\"," +
            "\"unique_id\": \"absoluta_partition_global\"," +
            "\"command_topic\": \"ABS/global/set\"," +
            "\"code_arm_required\": false," +
            "\"code_disarm_required\": false," +
            "\"supported_features\": [\"arm_away\"]," +
            "\"payload_arm_away\": \"ARM_AWAY\"," +
            "\"payload_disarm\": \"DISARM\"," +
            "\"device\": {" +
               "\"identifiers\": [\"absoluta_panel\"]," +
               "\"name\": \"Centrale Absoluta\"," +
               "\"manufacturer\": \"Bentel\"," +
               "\"model\": \"Absoluta\"" +
            "}" +
         "}";
         try {
            MqttMessage discoveryMsg = new MqttMessage(payload.getBytes());
            discoveryMsg.setQos(1);
            discoveryMsg.setRetained(true);
            this.mqttClient.publish(topic, discoveryMsg);
            partitionDiscoverySent.add(0);
         } catch (Exception ex) {
            System.out.println("ERROR: invio discovery partizione globale: " + topic);
         }
      }

      if (!this.isConnected){
         this.isConnected = true;
         try {
               MqttMessage mqttMessage = new MqttMessage("Status: Connesso".getBytes());
               mqttMessage.setQos(1);
               this.mqttClient.publish("ABS/conn", mqttMessage);
         } catch (Exception ex) {
            System.out.println("ERROR: invio messaggio: " + "ABS/conn");
            System.out.println("ERROR: " + ex.getMessage());
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
         this.partitionArmStatuses[0] = "disarmed";
      } else if (actArming == Arming.GLOBALLY_ARMED) {
         this.partitionArmStatuses[0] = "armed_away";
      } else if (actArming == Arming.PARTIALLY_ARMED) {
         this.partitionArmStatuses[0] = "armed_custom_bypass";
      }

      try {
         String str = "";
         if(discoveryEnabled){
            str = this.partitionArmStatuses[0];
         } else {
            str = "Name: " + this.partitionNames[0] + " Arming: " + this.partitionArmStatuses[0] + " Status: " + this.partitionStatuses[0];
         }
         MqttMessage msg = new MqttMessage(str.getBytes());
         msg.setQos(1);
         this.mqttClient.publish(this.partitionTopics[0], msg);
      } catch (Exception ex) {
         System.out.println("ERROR: invio messaggio: " + this.partitionTopics[0]);
         System.out.println("ERROR: " + ex.getMessage());
      }
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: Stato generale allarme cambiato in: " + actArming.toString());
      }
   }

   public void setInputRemoteName(String sensorID, String sensorName) {
      int sensorIDInt = Integer.parseInt(sensorID);
      this.sensorNames[sensorIDInt] = sensorName;
      this.sensorTopics[sensorIDInt] = "ABS/sensor/" + sensorIDInt;
      // Invia discovery solo la prima volta per ogni sensore
      if (discoveryEnabled && !sensorDiscoverySent.contains(sensorIDInt)) {
         String topic = "homeassistant/binary_sensor/absoluta_sensor_" + sensorIDInt + "/config";
         String payload = "{" +
            "\"name\": \"" + sensorName + "\"," +
            "\"state_topic\": \"ABS/sensor/" + sensorID + "\"," +
            "\"unique_id\": \"absoluta_sensor_" + sensorID + "\"," +
            "\"device_class\": \"motion\"," +
            "\"device\": {" +
               "\"identifiers\": [\"absoluta_panel\"]," +
               "\"name\": \"Centrale Absoluta\"," +
               "\"manufacturer\": \"Bentel\"," +
               "\"model\": \"Absoluta\"" +
            "}" +
         "}";
         try {
            MqttMessage discoveryMsg = new MqttMessage(payload.getBytes());
            discoveryMsg.setQos(1);
            discoveryMsg.setRetained(true);
            this.mqttClient.publish(topic, discoveryMsg);
            sensorDiscoverySent.add(sensorIDInt);
         } catch (Exception ex) {
            System.out.println("ERROR: invio discovery sensore: " + topic);
         }
         topic = "homeassistant/switch/absoluta_sensor_" + sensorIDInt + "_bypass/config";
         payload = "{" +
            "\"name\": \"" + sensorName + " Bypass\"," +
            "\"state_topic\": \"ABS/sensor/" + sensorID + "_bypass" + "\"," +
            "\"unique_id\": \"absoluta_sensor_" + sensorID + "_bypass\"," +
            "\"command_topic\": \"ABS/sensor/" + sensorID + "/set\"," +
            "\"payload_on\": \"ON\"," +
            "\"payload_off\": \"OFF\"," +
            "\"device_class\": \"switch\"," +
            "\"device\": {" +
               "\"identifiers\": [\"absoluta_panel\"]," +
               "\"name\": \"Centrale Absoluta\"," +
               "\"manufacturer\": \"Bentel\"," +
               "\"model\": \"Absoluta\"" +
            "}" +
         "}";
         try {
            MqttMessage discoveryMsg = new MqttMessage(payload.getBytes());
            discoveryMsg.setQos(1);
            discoveryMsg.setRetained(true);
            this.mqttClient.publish(topic, discoveryMsg);
         } catch (Exception ex) {
            System.out.println("ERROR: invio discovery sensore Bypass: " + topic);
         }
      }
      try {
         String str = "";
         if(discoveryEnabled){
            str = this.sensorStatuses[sensorIDInt].toUpperCase();
         } else {
            str = "Name: " + this.sensorNames[sensorIDInt] + " Status: " + this.sensorStatuses[sensorIDInt];
         }
         MqttMessage msg = new MqttMessage(str.getBytes());
         msg.setQos(1);
         this.mqttClient.publish(this.sensorTopics[sensorIDInt], msg);
      } catch (Exception ex) {
         System.out.println("ERROR: invio messaggio: " + this.sensorTopics[sensorIDInt]);
         System.out.println("ERROR: " + ex.getMessage());
      }
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: Sensore ID: " + sensorID + ", nome: " + sensorName);
      }
      try {
         this.mqttClient.subscribe(this.sensorTopics[sensorIDInt] + "/set");
      } catch (Exception ex) {
         System.out.println("ERROR: subscribe to: " + this.sensorTopics[sensorIDInt]);
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
            String str = "";
            if(discoveryEnabled){
               str = this.sensorStatuses[sensorIDInt].toUpperCase();
            } else {
               str = "Name: " + this.sensorNames[sensorIDInt] + " Status: " + this.sensorStatuses[sensorIDInt];
            }
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

      String bypassStatus = "";
      String topic = "ABS/sensor/" + sensorID + "_bypass";
      if (this.panel.getBypassInput(sensorID)) {
         bypassStatus = "ON";
      } else {
         bypassStatus = "OFF";
      }
      try {
            MqttMessage discoveryMsg = new MqttMessage(bypassStatus.getBytes());
            discoveryMsg.setQos(1);
            this.mqttClient.publish(topic, discoveryMsg);
         } catch (Exception ex) {
            System.out.println("ERROR: invio comando Bypass sensore: " + topic);
      }
   }

   public void setLabelArming(char modeChar, String modeLabel) {
      int modeIDInt = 0;
      switch (modeChar) {
         case 'A':
            modeIDInt = 0;
            if (modeLabel == null || modeLabel.trim().isEmpty()) {
               modeDiscoverySent.add(modeIDInt);
               break;
            }
            this.modeNames[0] = modeLabel;
            break;
         case 'B':
            modeIDInt = 1;
            if (modeLabel == null || modeLabel.trim().isEmpty()) {
               modeDiscoverySent.add(modeIDInt);
               break;
            }
            this.modeNames[1] = modeLabel;
            break;
         case 'C':
            modeIDInt = 2;
            if (modeLabel == null || modeLabel.trim().isEmpty()) {
               modeDiscoverySent.add(modeIDInt);
               break;
            }
            this.modeNames[2] = modeLabel;
            break;
         case 'D':
            modeIDInt = 3;
            if (modeLabel == null || modeLabel.trim().isEmpty()) {
               modeDiscoverySent.add(modeIDInt);
               break;
            }
            this.modeNames[3] = modeLabel;
            break;
         default:
            break;
      }
      // Invia discovery solo la prima volta per ogni modalità
      if (discoveryEnabled && !modeDiscoverySent.contains(modeIDInt)) {
         String topic = "homeassistant/button/absoluta_mode_" + modeChar + "/config";
         String payload = "{" +
         "\"name\": \"Mode " + modeLabel + "\"," +
         "\"state_topic\": \"ABS/mode/" + modeChar + "\"," +
         "\"unique_id\": \"absoluta_mode_" + modeChar + "\"," +
         "\"command_topic\": \"ABS/mode/" + modeChar + "/set\"," +
         "\"payload_press\": \"MODE_" + modeChar + "\"," +
         "\"device\": {" +
            "\"identifiers\": [\"absoluta_panel\"]," +
            "\"name\": \"Centrale Absoluta\"," +
            "\"manufacturer\": \"Bentel\"," +
            "\"model\": \"Absoluta\"" +
         "}" +
         "}";
         try {
            MqttMessage discoveryMsg = new MqttMessage(payload.getBytes());
            discoveryMsg.setQos(1);
            discoveryMsg.setRetained(true);
            modeDiscoverySent.add(modeIDInt);
            this.mqttClient.publish(topic, discoveryMsg);
         } catch (Exception ex) {
            System.out.println("ERROR: invio discovery sensore Bypass: " + topic);
         }
      }

      try {
         this.mqttClient.subscribe("ABS/mode");
      } catch (Exception ex) {
         System.out.println("ERROR: subscribe to: " + "ABS/mode");
      }
   }

   public void setOutputRemoteName(String var1, String var2) {
   }

   public void setOutputStatus(String var1, Output.Status var2) {
   }

   public void setPartitionArming(String partitionID, Partition.Arming actArming) {
      int partitionIDInt = Integer.parseInt(partitionID);
      if (actArming == cms.device.api.Partition.Arming.DISARMED) {
         this.partitionArmStatuses[partitionIDInt] = "disarmed";
      } else if (actArming == cms.device.api.Partition.Arming.AWAY) {
         this.partitionArmStatuses[partitionIDInt] = "armed_away";
      } else if (actArming == cms.device.api.Partition.Arming.STAY) {
         this.partitionArmStatuses[partitionIDInt] = "armed_home";
      } else if (actArming == cms.device.api.Partition.Arming.NODELAY) {
         this.partitionArmStatuses[partitionIDInt] = "armed_night";
      } else if (actArming == cms.device.api.Partition.Arming.TRIGGERED) {
         this.partitionArmStatuses[partitionIDInt] = "triggered";
      }

      if (this.partitionNames[partitionIDInt] != null) {
         try {
            String str = "";
            if(discoveryEnabled){
               str = this.partitionArmStatuses[partitionIDInt];
            } else {
               str = "Name: " + this.partitionNames[partitionIDInt] + " Arming: " + this.partitionArmStatuses[partitionIDInt] + " Status: " + this.partitionStatuses[partitionIDInt];
            }
            MqttMessage msg = new MqttMessage(str.getBytes());
            msg.setQos(1);
            this.mqttClient.publish(this.partitionTopics[partitionIDInt], msg);
         } catch (Exception ex) {
            System.out.println("ERROR: invio messaggio: " + this.partitionTopics[partitionIDInt]);
            System.out.println("ERROR: " + ex.getMessage());
         }
      }
      if(VERBOSE_DEBUG) {
         System.out.println("Partizione ID: " + partitionID + " stato arming: " + actArming.toString());
      }
   }

   public void setPartitionRemoteName(String partitionID, String partitionName) {
      int partitionIDInt = Integer.parseInt(partitionID);
      this.partitionNames[partitionIDInt] = partitionName;
      this.partitionTopics[partitionIDInt] = "ABS/partition/" + (partitionIDInt);

      // Invia discovery solo la prima volta per ogni partizione
      if (discoveryEnabled && !partitionDiscoverySent.contains(partitionIDInt)  && partitionIDInt > 0) {
         String topic = "homeassistant/alarm_control_panel/absoluta_partition_" + partitionID + "/config";
         String payload = "{" +
            "\"name\": \"" + partitionName + "\"," +
            "\"state_topic\": \"ABS/partition/" + partitionID + "\"," +
            "\"unique_id\": \"absoluta_partition_" + partitionID + "\"," +
            "\"command_topic\": \"ABS/partition/" + partitionID + "/set\"," +
            "\"code_arm_required\": false," +
            "\"code_disarm_required\": false," +
            "\"supported_features\": [\"arm_home\", \"arm_away\", \"arm_night\"]," +
            "\"payload_arm_away\": \"ARM_AWAY\"," +
            "\"payload_arm_home\": \"ARM_HOME\"," +
            "\"payload_arm_night\": \"ARM_NIGHT\"," +
            "\"payload_disarm\": \"DISARM\"," +
            "\"device\": {" +
               "\"identifiers\": [\"absoluta_panel\"]," +
               "\"name\": \"Centrale Absoluta\"," +
               "\"manufacturer\": \"Bentel\"," +
               "\"model\": \"Absoluta\"" +
            "}" +
      "}";
         try {
            MqttMessage discoveryMsg = new MqttMessage(payload.getBytes());
            discoveryMsg.setQos(1);
            discoveryMsg.setRetained(true);
            this.mqttClient.publish(topic, discoveryMsg);
            partitionDiscoverySent.add(partitionIDInt);
         } catch (Exception ex) {
            System.out.println("ERROR: invio discovery partizione: " + topic);
         }
      }

      try{
         this.mqttClient.subscribe(this.partitionTopics[partitionIDInt] + "/set");
      } catch (Exception ex) {
         System.out.println("ERROR: subscribe to: " + this.partitionTopics[partitionIDInt]);
      }

      try {
         String str = "";
         if(discoveryEnabled){
            str = this.partitionNames[partitionIDInt];
         } else {
            str = "Name: " + this.partitionNames[partitionIDInt] + " Arming: " + this.partitionArmStatuses[partitionIDInt] + " Status: " + this.partitionStatuses[partitionIDInt];
         }
         MqttMessage msg = new MqttMessage(str.getBytes());
         msg.setQos(1);
         this.mqttClient.publish(this.partitionTopics[partitionIDInt], msg);
      } catch (Exception ex) {
         System.out.println("ERROR: invio messaggio: " + this.partitionTopics[partitionIDInt]);
         System.out.println("ERROR: " + ex.getMessage());
      }
      if(VERBOSE_DEBUG) {
         System.out.println("Partizione ID: " + partitionID + ", nome: " + partitionName);
      }
   }

   public void setPartitionStatus(String partitionID, Partition.Status actStatus) {
      int partitionIDInt = Integer.parseInt(partitionID);
      switch(actStatus) {
         case Partition.Status.FIRE:
            this.partitionStatuses[partitionIDInt] = "Fire";
            break;
         case Partition.Status.FAULTS:
            this.partitionStatuses[partitionIDInt] = "Faults";
            break;
         case Partition.Status.ALARMS:
            this.partitionStatuses[partitionIDInt] = "Alarms";
            break;
         case Partition.Status.OK:
            this.partitionStatuses[partitionIDInt] = "Ok";
            break;
         default:
            break;
      }

      if (this.partitionNames[partitionIDInt] != null) {
         try {
            String str = "";
            if(discoveryEnabled){
               str = this.partitionStatuses[partitionIDInt].toUpperCase();
            } else {
               str = "Name: " + this.partitionNames[partitionIDInt] + " Arming: " + this.partitionArmStatuses[partitionIDInt] + " Status: " + this.partitionStatuses[partitionIDInt];
            }
            MqttMessage msg = new MqttMessage(str.getBytes());
            msg.setQos(1);
            this.mqttClient.publish(this.partitionTopics[partitionIDInt], msg);
         } catch (Exception ex) {
            System.out.println("ERROR: invio messaggio: " + this.partitionTopics[partitionIDInt]);
            System.out.println("ERROR: " + ex.getMessage());
         }
      }
      if(VERBOSE_DEBUG) {
         System.out.println("Partizione ID: " + partitionID + " stato cambiato in: " + actStatus.toString());
      }
   }

   public void setPartitionsArming(Partition.Arming var1) {
   }

   public void setRemoteName(String var1) {
   }

   public void setStatus(Panel.Status var1) {
   }

   public void tagInputIntoPartition(String var1, List<String> var2) {
   }

   // Smistamento dei comandi ricevuti via MQTT
   public void messageArrived(String topic, MqttMessage msg) {
      String parentTopic = "";
      if(topic.startsWith("ABS/") && topic.endsWith("/set")) {
         parentTopic = topic.replace("/set", "");
         if (ArrayUtils.contains(this.partitionTopics, parentTopic)) {
            int idArray = ArrayUtils.indexOf(this.partitionTopics, parentTopic);
            if (idArray > 0 && idArray <= this.partitionIDs.length) {
               // Se partizione
               this.commandPartition(idArray, msg);
            } else if (idArray == 0) {
               // Se globale
               this.commandGlobal(idArray, msg);
            } else {
               // Errore
               System.out.println("WARN: ID " + idArray + " non valido per il topic: " + topic);
            }
         } else if (ArrayUtils.contains(this.sensorTopics, parentTopic)) {
            int idSensor = ArrayUtils.indexOf(this.sensorTopics, parentTopic);
            int idArray = ArrayUtils.indexOf(this.sensorIDs, idSensor);
            if (idArray >= 0 && idArray < this.sensorIDs.length) {
               // Se sensore
               this.commandSensor(idArray, msg);
            } else {
               // Errore
               System.out.println("WARN: ID " + idArray + " non valido per il topic: " + topic);
            }
         } else if (parentTopic.contains("mode")) {
            this.commandMode(msg);
         }
      } else if (topic.equals("homeassistant/status")) {
         //TODO: #ALESSANDRO Gestione del reinvio degli stati quando home assistant si riavvia
      } else {
         // Comando non riconosciuto
         System.out.println("WARN: Comando non riconosciuto per il topic: " + topic);
      }
   }

   private void commandPartition(int idArray, MqttMessage msg) {
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: Comando ricevuto per partizione numero: " + idArray + " nuovo stato: " + msg.toString());
      }
      switch (msg.toString().toUpperCase()) {
         case "DISARM":
            this.panel.partitionArming(String.valueOf(this.partitionIDs[idArray]), cms.device.api.Partition.Arming.DISARMED);
            return;
         case "ARM_HOME":
            this.panel.partitionArming(String.valueOf(this.partitionIDs[idArray]), cms.device.api.Partition.Arming.STAY);
            return;
         case "ARM_AWAY":
            this.panel.partitionArming(String.valueOf(this.partitionIDs[idArray]), cms.device.api.Partition.Arming.AWAY);
            return;
         case "ARM_NIGHT":
            this.panel.partitionArming(String.valueOf(this.partitionIDs[idArray]), cms.device.api.Partition.Arming.NODELAY);
            return;
         default:
            System.out.println("WARN: Comando " + msg.toString() + " non valido");
      }
   }

   private void commandGlobal(int idArray, MqttMessage msg) {
      if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: Comando ricevuto per stato globale: " + msg.toString());
         }
         switch (msg.toString().toUpperCase()) {
            case "DISARM":
               this.panel.arming(Arming.GLOBALLY_DISARMED);
               return;
            case "ARM_AWAY":
               this.panel.arming(Arming.GLOBALLY_ARMED);
               return;
            default:
               System.out.println("WARN: Comando " + msg.toString() + " non valido");
         }
   }

   private void commandMode(MqttMessage msg) {
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: Comando ricevuto per modalità: " + msg.toString());
      }
      switch (msg.toString().toUpperCase()) {
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
   }

   private void commandSensor(int idArray, MqttMessage msg) {
      if(msg.toString().equals("ON")) {
         this.panel.bypassInput(String.valueOf(this.sensorIDs[idArray]), true);
      } else if(msg.toString().equals("OFF")){
         this.panel.bypassInput(String.valueOf(this.sensorIDs[idArray]), false);
      } else {
         System.out.println("WARN: Comando " + msg.toString() + " non valido per il sensore ID: " + idArray);
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
            ConnStatus var2 = this.panel.connect();
            if (var2 == ConnStatus.UNREACHABLE) {
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