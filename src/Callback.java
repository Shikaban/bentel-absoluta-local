import java.util.List;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import absoluta.AbsolutaPanelProvider;
import absoluta.AbsolutaPanelProvider.providerStatus;
import absoluta.connection.PanelStatus;

import java.util.logging.Logger;

class Callback implements AbsolutaPanelProvider.PanelCallback, MqttCallback {
   private static final Logger logger = Logger.getLogger(Callback.class.getName());
   private MqttClient mqttClient;
   private final Entities entities = new Entities();
   private final BridgeAlertManager bridgeAlertManager;
   private final EntityManager entityManager;
   private final CommandManager commandManager;
   private final Publisher publisher;
   private final ConnectionManager connectionManager;
   private AbsolutaPanelProvider provider;
   private static final int QOS = 1;
   private Boolean discoveryEnabled;
   private MqttMessageDispatcher mqttDispatcher;

   public Callback(MqttClient mqttClient, AbsolutaPanelProvider provider, MqttConnectOptions mqttOption,
         Boolean discoveryEnabled) {
      this.mqttClient = mqttClient;
      this.provider = provider;
      this.discoveryEnabled = discoveryEnabled;
      this.mqttDispatcher = new MqttMessageDispatcher(mqttClient);

      boolean discovery = Boolean.TRUE.equals(discoveryEnabled);
      this.publisher = new Publisher(this.mqttDispatcher, logger);
      this.bridgeAlertManager = new BridgeAlertManager(publisher, QOS);
      this.entityManager = new EntityManager(entities, mqttClient, publisher, discovery, logger, QOS, bridgeAlertManager);
      this.commandManager = new CommandManager(entities, provider, bridgeAlertManager, entityManager, logger);
      this.connectionManager = new ConnectionManager(mqttClient, mqttOption, provider, publisher, logger, QOS, entityManager);
   }

   public void connectionLost(Throwable ex) {
      connectionManager.onMqttConnectionLost(ex);
   }

   public void connectionLost() {
      bridgeAlertManager.notifyAlert("Connessione persa con la centrale! Riconnessione...");
      connectionManager.onPanelConnectionLost();
   }

   public void alert(String message) {
      // Questa è la callback chiamata da AlertListener per notificare errori
      // specifici
      // come problemi di inserimento, esclusione zone e controllo output
      logger.warning("Alert received: " + message);

      bridgeAlertManager.notifyAlert(message);
   }

   public void setStatus(providerStatus status) {
   }

   public void notifyError(String errorMessage) {
      bridgeAlertManager.notifyAlert(errorMessage);
   }

   public void getAllZones(List<Integer> zones) {
      entityManager.initZones(zones);
   }

   public void getAllOutputs(List<Integer> outputs) {
   }

   public void getAllPartitions(List<Integer> partitions) {
      entityManager.initPartitions(partitions);

      connectionManager.onPanelConnected();
      logger.fine("Partizione ID: " + String.valueOf(partitions));
   }

   public void updateGlobalArming(PanelStatus.GlobalArming actArming) {
      entityManager.updateGlobalArming(actArming);
   }

   public void updateZoneName(int zoneId, String zoneName) {
      entityManager.updateZoneName(zoneId, zoneName);
   }

   public void updateZoneStatus(int zoneId, PanelStatus.InputStatus zoneStatus) {
      entityManager.updateZoneStatus(zoneId, zoneStatus);
   }

   public void updateZoneBypass(int zoneId, boolean bypass) {
      entityManager.updateZoneBypass(zoneId, bypass);
   }

   public void updateModeLabel(char modeChar, String modeLabel) {
      entityManager.updateModeLabel(modeChar, modeLabel);
   }

   public void updatePanelFaults(List<String> panelFaults) {
      entityManager.updatePanelFaults(panelFaults);
   }

   public void updateAlarmMemory(List<String> partitionsWithAlarmMemory) {
      entityManager.updateAlarmMemory(partitionsWithAlarmMemory);
   }

   public void updateOutputName(int outputID, String name) {
   }

   public void updateOutputStatus(int outputID, PanelStatus.OutputStatus status) {
   }

   public void updatePartitionArming(int partitionID, PanelStatus.PartitionArming actArming) {
      entityManager.updatePartitionArming(partitionID, actArming);
   }

   public void updatePartitionName(int partitionID, String name) {
      entityManager.updatePartitionName(partitionID, name);
   }

   public void updatePartitionStatus(int partitionID, PanelStatus.PartitionStatus actStatus) {
      entityManager.updatePartitionStatus(partitionID, actStatus);
   }

   public void tagZoneIntoPartition(int partitionId, List<Integer> zones) {
   }

   // Smistamento dei comandi ricevuti via MQTT
   public void messageArrived(String topic, MqttMessage msg) {
      commandManager.onMessageArrived(topic, msg);
   }

   public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken2) {
   }

}