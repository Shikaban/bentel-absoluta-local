import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import absoluta.AbsolutaPanelProvider;
import absoluta.connection.PanelStatus;

final class CommandManager {
    private static final Map<String, PanelStatus.PartitionArming> PARTITION_COMMANDS = Map.of(
            "DISARM", PanelStatus.PartitionArming.DISARMED,
            "ARM_HOME", PanelStatus.PartitionArming.STAY,
            "ARM_AWAY", PanelStatus.PartitionArming.AWAY,
            "ARM_NIGHT", PanelStatus.PartitionArming.NODELAY);

    private static final Map<String, PanelStatus.GlobalArming> GLOBAL_COMMANDS = Map.of(
            "DISARM", PanelStatus.GlobalArming.GLOBALLY_DISARMED,
            "ARM_AWAY", PanelStatus.GlobalArming.GLOBALLY_ARMED);

    private static final Map<String, Character> MODE_COMMANDS = Map.of(
            "MODE_A", 'A',
            "MODE_B", 'B',
            "MODE_C", 'C',
            "MODE_D", 'D');

    private final Entities entities;
    private final AbsolutaPanelProvider provider;
    private final BridgeAlertManager bridgeAlertManager;
    private final EntityManager entityManager;
    private final Logger logger;

    CommandManager(Entities entities, AbsolutaPanelProvider provider, BridgeAlertManager bridgeAlertManager,
            EntityManager entityManager, Logger logger) {
        this.entities = entities;
        this.provider = provider;
        this.bridgeAlertManager = bridgeAlertManager;
        this.entityManager = entityManager;
        this.logger = logger;
    }

    void onMessageArrived(String topic, MqttMessage msg) {
        if (topic == null) {
            return;
        }

        if (topic.startsWith("ABS/") && topic.endsWith("/set")) {
            String parentTopic = topic.replace("/set", "");
            Integer partitionId = entities.partitionIdByTopic.get(parentTopic);
            if (partitionId != null) {
                if (partitionId == 0) {
                    commandGlobal(msg);
                } else {
                    commandPartition(partitionId, msg);
                }
                return;
            }

            Integer zoneId = entities.sensorIdByTopic.get(parentTopic);
            if (zoneId != null) {
                commandSensor(zoneId, msg);
                return;
            }

            if (parentTopic.contains("mode")) {
                commandMode(msg);
                return;
            }

            if (parentTopic.contains("absoluta_bridge_alerts")) {
                if (msg != null && msg.toString().equals("RESET_ERRORS")) {
                    logger.info("Resetting bridge alerts...");
                    provider.cleanTroubles();
                    bridgeAlertManager.resetAlerts();
                } else {
                    logger.warning("Command " + (msg == null ? "null" : msg.toString()) + " is invalid for topic: "
                            + topic);
                }
                return;
            }

            if (parentTopic.contains("absoluta_alarm_memory")) {
                if (msg != null && msg.toString().equals("RESET_ALARM_MEMORY")) {
                    logger.info("Resetting alarm memory...");
                    provider.cleanAlarmMemory();
                } else {
                    logger.warning("Command " + (msg == null ? "null" : msg.toString()) + " is invalid for topic: "
                            + topic);
                }
                return;
            }

            logger.warning("Unrecognized command for topic: " + topic);
            return;
        }

        if (topic.equals("homeassistant/status")) {
            if (msg != null && msg.toString().equals("online")) {
                entityManager.onHomeAssistantOnline();
            }
            return;
        }

        logger.warning("Unrecognized command for topic: " + topic);
    }

    private void commandPartition(int partitionId, MqttMessage msg) {
        logger.fine("Command received for partition number " + partitionId + ", new state: "
                + (msg == null ? "null" : msg.toString()));
        String cmd = normalizeCmd(msg);
        PanelStatus.PartitionArming arming = PARTITION_COMMANDS.get(cmd);
        if (arming == null) {
            logger.warning("Invalid command " + cmd);
            return;
        }
        if (arming != PanelStatus.PartitionArming.DISARMED) {
            if (rejectIfNotReady("partition " + partitionId, partitionId)) {
                return;
            }
        }
        provider.setPartitionArming(partitionId, arming);
    }

    private void commandGlobal(MqttMessage msg) {
        logger.fine("Command received for global state: " + (msg == null ? "null" : msg.toString()));
        String cmd = normalizeCmd(msg);
        PanelStatus.GlobalArming arming = GLOBAL_COMMANDS.get(cmd);
        if (arming == null) {
            logger.warning("Invalid command " + cmd);
            return;
        }
        if (arming == PanelStatus.GlobalArming.GLOBALLY_ARMED) {
            int[] partIds = realPartitionIds();
            if (rejectIfNotReady("global", partIds)) {
                return;
            }
        }
        provider.setGlobalArming(arming);
    }

    private void commandMode(MqttMessage msg) {
        logger.fine("Command received for mode: " + (msg == null ? "null" : msg.toString()));
        String cmd = normalizeCmd(msg);
        Character mode = MODE_COMMANDS.get(cmd);
        if (mode == null) {
            logger.warning("Invalid command " + cmd);
            return;
        }
        if (rejectIfNotReady("mode " + mode, realPartitionIds())) {
            return;
        }
        provider.setModeArming(mode);
    }

    private void commandSensor(int zoneId, MqttMessage msg) {
        if (msg == null) {
            logger.warning("Null command is invalid for sensor ID: " + zoneId);
            return;
        }

        if (msg.toString().equals("ON")) {
            provider.setZoneBypass(zoneId, true);
        } else if (msg.toString().equals("OFF")) {
            provider.setZoneBypass(zoneId, false);
        } else {
            logger.warning("Command " + msg.toString() + " is invalid for sensor ID: " + zoneId);
        }
    }

    private boolean rejectIfNotReady(String context, int... partitionIds) {
        for (int partitionId : partitionIds) {
            Boolean ready = provider.isPartitionReady(partitionId);
            if (!Boolean.TRUE.equals(ready)) {
                String msgText = "Arming rejected (" + context + "): partition " + partitionId
                        + " is not ready (zones open or in fault)";
                logger.warning(msgText);
                bridgeAlertManager.notifyAlert(msgText);
                return true;
            }
        }
        return false;
    }

    private int[] realPartitionIds() {
        List<Integer> ids = new ArrayList<>();
        for (Integer id : entities.partitionsById.keySet()) {
            if (id != 0) {
                ids.add(id);
            }
        }
        return ids.stream().mapToInt(Integer::intValue).toArray();
    }

    private static String normalizeCmd(MqttMessage msg) {
        return msg == null ? "" : msg.toString().trim().toUpperCase();
    }
}
