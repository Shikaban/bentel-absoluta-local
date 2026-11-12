import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


import org.eclipse.paho.client.mqttv3.MqttClient;

import absoluta.connection.PanelStatus;

final class EntityManager {
    private static final EnumSet<PanelStatus.PartitionStatus> PARTITION_STATUS_UPDATES = EnumSet.of(
            PanelStatus.PartitionStatus.FIRE,
            PanelStatus.PartitionStatus.FAULTS,
            PanelStatus.PartitionStatus.ALARMS,
            PanelStatus.PartitionStatus.OK);

    private final Entities entities;
    private final MqttClient mqttClient;
    private final Publisher publish;
    private final boolean discoveryEnabled;
    private final Logger logger;
    private final int qos;
    private final BridgeAlertManager bridgeAlertManager;

    EntityManager(Entities entities, MqttClient mqttClient, Publisher publish, boolean discoveryEnabled, Logger logger,
            int qos, BridgeAlertManager bridgeAlertManager) {
        this.entities = entities;
        this.mqttClient = mqttClient;
        this.publish = publish;
        this.discoveryEnabled = discoveryEnabled;
        this.logger = logger;
        this.qos = qos;
        this.bridgeAlertManager = bridgeAlertManager;
    }

    void initZones(List<Integer> zones) {
        entities.sensorsById.clear();
        entities.sensorIdByTopic.clear();

        for (int zoneId : zones) {
            Entities.Sensor sensor = new Entities.Sensor();
            sensor.topic = "ABS/sensor/" + zoneId;
            sensor.bypass = "OFF";
            entities.sensorsById.put(zoneId, sensor);
            entities.sensorIdByTopic.put(sensor.topic, zoneId);
        }

        logger.fine("Sensore ID: " + zones);
    }

    void initPartitions(List<Integer> partitions) {
        entities.partitionsById.clear();
        entities.partitionIdByTopic.clear();

        Entities.Partition global = new Entities.Partition();
        global.name = "Globale";
        global.topic = "ABS/partition/0";
        entities.partitionsById.put(0, global);
        entities.partitionIdByTopic.put(global.topic, 0);

        for (int partitionId : partitions) {
            Entities.Partition partition = new Entities.Partition();
            partition.topic = "ABS/partition/" + partitionId;
            entities.partitionsById.put(partitionId, partition);
            entities.partitionIdByTopic.put(partition.topic, partitionId);
        }

        if (discoveryEnabled && !entities.partitionDiscoverySent.contains(0)) {
            String topic = "homeassistant/alarm_control_panel/absoluta_partition_0/config";
            String payload = HomeAssistantManager.buildPartition(0, global.name);
            publish.publish(topic, payload, qos, true, "discovery partizione globale");
            entities.partitionDiscoverySent.add(0);

            topic = "homeassistant/button/absoluta_bridge_alerts_reset/config";
            payload = HomeAssistantManager.buildResetErrors();
            publish.publish(topic, payload, qos, true, "discovery reset errors");

            topic = "homeassistant/button/absoluta_alarm_memory_reset/config";
            payload = HomeAssistantManager.buildResetAlarmMemory();
            publish.publish(topic, payload, qos, true, "discovery reset alarm memory");

            topic = "homeassistant/sensor/absoluta_bridge_alerts/config";
            payload = HomeAssistantManager.buildErrorSensor();
            publish.publish(topic, payload, qos, true, "discovery errori");

            topic = "homeassistant/sensor/absoluta_panel_faults/config";
            payload = HomeAssistantManager.buildTroublesSensor();
            publish.publish(topic, payload, qos, true, "discovery guasti");
            publishPanelFaults();

            try {
                mqttClient.subscribe("homeassistant/status");
            } catch (Exception ex) {
                logger.warning("Subscribe to: homeassistant/status");
            }
        }

        logger.fine("Partizione ID: " + String.valueOf(partitions));
    }

    void updateGlobalArming(PanelStatus.GlobalArming actArming) {
        Entities.Partition global = entities.partitionsById.get(0);
        if (global != null && actArming != null) {
            global.arming = actArming.toString();
        }
        publishGlobalState();
    }

    void publishGlobalState() {
        Entities.Partition global = entities.partitionsById.get(0);
        if (global == null || global.topic == null) {
            logger.warning("Partizione globale non inizializzata");
            return;
        }

        String str;
        if (discoveryEnabled) {
            str = global.arming == null ? "disarmed" : global.arming;
        } else {
            str = "Name: " + global.name + " Arming: " + global.arming + " Status: " + global.status;
        }

        publish.publish(global.topic, str, qos, true, "stato partizione globale");
        logger.fine("Partition Name: " + global.name + " Arming: " + global.arming + " Status: " + global.status);
    }

    void updateZoneName(int zoneId, String zoneName) {
        Entities.Sensor sensor = entities.sensorsById.get(zoneId);
        if (sensor == null) {
            logger.warning("Sensore non inizializzato: " + zoneId);
            return;
        }
        if (!isValidLabel(zoneName)) {
            logger.warning("Nome zona non valido: " + zoneId);
            return;
        }

        sensor.name = zoneName.trim();
        if (sensor.topic == null) {
            sensor.topic = "ABS/sensor/" + zoneId;
            entities.sensorIdByTopic.put(sensor.topic, zoneId);
        }
        if (sensor.bypass == null) {
            sensor.bypass = "OFF";
        }

        if (discoveryEnabled && !entities.sensorDiscoverySent.contains(zoneId)) {
            String topic = "homeassistant/binary_sensor/absoluta_sensor_" + zoneId + "/config";
            String payload = HomeAssistantManager.buildSensor(zoneId, sensor.name);
            publish.publish(topic, payload, qos, true, "discovery sensore");
            entities.sensorDiscoverySent.add(zoneId);

            topic = "homeassistant/switch/absoluta_sensor_" + zoneId + "_bypass/config";
            payload = HomeAssistantManager.buildSensorBypass(zoneId, sensor.name);
            publish.publish(topic, payload, qos, true, "discovery sensore Bypass");
        }

        publishZoneState(zoneId);
        logger.fine("Sensor Name: " + sensor.name + " Status: " + sensor.status + " Bypass: " + sensor.bypass);
    }

    void updateZoneStatus(int zoneId, PanelStatus.InputStatus zoneStatus) {
        Entities.Sensor sensor = entities.sensorsById.get(zoneId);
        if (sensor == null) {
            logger.warning("Sensore non inizializzato: " + zoneId);
            return;
        }

        sensor.status = (zoneStatus != PanelStatus.InputStatus.ACTIVE && zoneStatus != PanelStatus.InputStatus.ALARM)
                ? "Off"
                : "On";
        publishZoneState(zoneId);
    }

    void updateZoneBypass(int zoneId, boolean bypass) {
        Entities.Sensor sensor = entities.sensorsById.get(zoneId);
        if (sensor == null) {
            logger.warning("Sensore non inizializzato: " + zoneId);
            return;
        }

        sensor.bypass = bypass ? "ON" : "OFF";
        publishZoneState(zoneId);
    }

    void publishZoneState(int zoneId) {
        Entities.Sensor sensor = entities.sensorsById.get(zoneId);
        if (sensor == null) {
            logger.warning("Sensore non inizializzato: " + zoneId);
            return;
        }
        if (sensor.name == null) {
            logger.finest("Nome sensore nullo: " + zoneId + ", non invio stato.");
            return;
        }

        String str;
        if (discoveryEnabled) {
            String bypassTopic = "ABS/sensor/" + zoneId + "_bypass";
            String bypassValue = sensor.bypass == null ? "OFF" : sensor.bypass.toUpperCase();
            publish.publish(bypassTopic, bypassValue, qos, true, "bypass sensore");
            str = sensor.status == null ? "OFF" : sensor.status.toUpperCase();
        } else {
            str = "Name: " + sensor.name + " Status: " + sensor.status + " Bypass: " + sensor.bypass;
        }

        publish.publish(sensor.topic, str, qos, true, "stato sensore");
        logger.fine("Sensor Name: " + sensor.name + " Status: " + sensor.status + " Bypass: " + sensor.bypass);
    }

    void updateModeLabel(char modeChar, String modeLabel) {
        int modeIDInt = modeChar - 'A';
        if (modeIDInt < 0 || modeIDInt >= entities.modeNames.length) {
            logger.warning("Modalità non valida: " + modeChar);
            return;
        }
        if (!isValidLabel(modeLabel)) {
            logger.warning("Nome modalità non valido: " + modeChar);
            return;
        }

        entities.modeNames[modeIDInt] = modeLabel.trim();
        if (discoveryEnabled && !entities.modeDiscoverySent.contains(modeIDInt)) {
            String topic = "homeassistant/button/absoluta_mode_" + modeIDInt + "/config";
            String payload = HomeAssistantManager.buildMode(modeChar, modeLabel);
            publish.publish(topic, payload, qos, true, "discovery modalità");
            entities.modeDiscoverySent.add(modeIDInt);
        }
    }

    void updatePartitionArming(int partitionID, PanelStatus.PartitionArming actArming) {
        Entities.Partition partition = entities.partitionsById.get(partitionID);
        if (partition == null) {
            logger.warning("Partizione non inizializzata: " + partitionID);
            return;
        }
        if (actArming != null) {
            partition.arming = actArming.toString();
        }
        publishPartitionArming(partitionID);
    }

    void publishPartitionArming(int partitionID) {
        Entities.Partition partition = entities.partitionsById.get(partitionID);
        if (partition == null || partition.name == null) {
            logger.warning("Indice partizione fuori dai limiti o nome nullo: " + partitionID);
            return;
        }

        String str;
        if (discoveryEnabled) {
            str = partition.arming == null ? "disarmed" : partition.arming;
        } else {
            str = "Name: " + partition.name + " Arming: " + partition.arming + " Status: " + partition.status;
        }

        publish.publish(partition.topic, str, qos, true, "stato partizione");
        logger.fine(
                "Partition Name: " + partition.name + " Arming: " + partition.arming + " Status: " + partition.status);
    }

    void updatePartitionName(int partitionID, String name) {
        Entities.Partition partition = entities.partitionsById.computeIfAbsent(partitionID,
                id -> new Entities.Partition());
        if (!isValidLabel(name)) {
            logger.warning("Nome partizione non valido: " + partitionID);
            return;
        }

        partition.name = name.trim();
        if (partition.topic == null) {
            partition.topic = "ABS/partition/" + partitionID;
        }
        entities.partitionIdByTopic.put(partition.topic, partitionID);

        if (discoveryEnabled && !entities.partitionDiscoverySent.contains(partitionID) && partitionID > 0) {
            String topic = "homeassistant/alarm_control_panel/absoluta_partition_" + partitionID + "/config";
            String payload = HomeAssistantManager.buildPartition(partitionID, name);
            publish.publish(topic, payload, qos, true, "discovery partizione");
            entities.partitionDiscoverySent.add(partitionID);
        }

        String str;
        if (discoveryEnabled) {
            str = partition.arming == null ? "disarmed" : partition.arming;
        } else {
            str = "Name: " + partition.name + " Arming: " + partition.arming + " Status: " + partition.status;
        }

        publish.publish(partition.topic, str, qos, true, "stato partizione");
        logger.fine(
                "Partition Name: " + partition.name + " Arming: " + partition.arming + " Status: " + partition.status);
    }

    void updatePartitionStatus(int partitionID, PanelStatus.PartitionStatus actStatus) {
        Entities.Partition partition = entities.partitionsById.get(partitionID);
        if (partition == null) {
            logger.warning("Partizione non inizializzata: " + partitionID);
            return;
        }
        if (actStatus != null && PARTITION_STATUS_UPDATES.contains(actStatus)) {
            partition.status = actStatus.toString();
        }

        if (partition.name != null) {
            String str;
            if (discoveryEnabled) {
                str = partition.arming == null ? "disarmed" : partition.arming;
            } else {
                str = "Name: " + partition.name + " Arming: " + partition.arming + " Status: " + partition.status;
            }
            publish.publish(partition.topic, str, qos, true, "stato partizione");
        }

        logger.fine(
                "Partition Name: " + partition.name + " Arming: " + partition.arming + " Status: " + partition.status);
    }

    void updatePanelFaults(List<String> panelFaults) {
        entities.panelFaults = panelFaults;
        publishPanelFaults();
    }

    void updateAlarmMemory(List<String> alarmMemory) {
        entities.alarmMemory = alarmMemory;
        publishPanelFaults();
    }

    private void publishPanelFaults() {
        List<String> panelFaults = entities.panelFaults;
        List<String> alarmMemory = entities.alarmMemory;
        int faultCount = panelFaults.size();
        int alarmCount = alarmMemory.size();

        String state;
        if (faultCount == 0 && alarmCount == 0) {
            state = "Nessun Problema";
        } else {
            List<String> parts = new ArrayList<>();
            if (faultCount == 1) parts.add("1 guasto attivo");
            else if (faultCount > 1) parts.add(faultCount + " guasti attivi");
            if (alarmCount == 1) parts.add("1 allarme in memoria");
            else if (alarmCount > 1) parts.add(alarmCount + " allarmi in memoria");
            state = String.join(", ", parts);
        }

        String attributes = "{\"guasti\":" + toJsonStringArray(panelFaults) + ",\"allarmi_in_memoria\":" + toJsonStringArray(alarmMemory) + "}";

        publish.publish("ABS/panel_faults", state, qos, true, "stato guasti centrale");
        publish.publish("ABS/panel_faults/attributes", attributes, qos, true, "attributi guasti centrale");
    }

    void onHomeAssistantOnline() {
        logger.fine("Comando ricevuto per Home Assistant online");
        republishAllStates();
    }

    void republishAllStates() {
        publishGlobalState();

        for (Map.Entry<Integer, Entities.Partition> entry : entities.partitionsById.entrySet()) {
            int partitionId = entry.getKey();
            if (partitionId == 0) {
                continue;
            }
            Entities.Partition partition = entry.getValue();
            if (partition != null && partition.name != null) {
                publishPartitionArming(partitionId);
            }
        }

        for (Map.Entry<Integer, Entities.Sensor> entry : entities.sensorsById.entrySet()) {
            int zoneId = entry.getKey();
            Entities.Sensor sensor = entry.getValue();
            if (sensor != null && sensor.name != null) {
                publishZoneState(zoneId);
            }
        }

        publishPanelFaults();
        bridgeAlertManager.publishAlerts();
    }

    private static String toJsonStringArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i).replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static boolean isValidLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            return false;
        }
        return label.chars().allMatch(ch -> ch >= 32 && ch <= 126);
    }
}
