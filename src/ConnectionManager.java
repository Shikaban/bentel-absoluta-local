import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import absoluta.AbsolutaPanelProvider;
import absoluta.AbsolutaPanelProvider.providerConnStatus;

final class ConnectionManager {
    private static final int RECON_DELAY_SECONDS = 90;
    private static final int MAX_MQTT_RECONNECT_ATTEMPTS = 10;

    private final MqttClient mqttClient;
    private final MqttConnectOptions mqttConnOpts;
    private final AbsolutaPanelProvider provider;
    private final Publisher publisher;
    private final Logger logger;
    private final int qos;
    private final EntityManager entityManager;

    private int panelReconnectionAttempts = 0;
    private int mqttReconnectAttempts = 0;
    private boolean isPanelConnected = false;

    ConnectionManager(MqttClient mqttClient, MqttConnectOptions mqttConnOpts, AbsolutaPanelProvider provider,
            Publisher publisher, Logger logger, int qos, EntityManager entityManager) {
        this.mqttClient = mqttClient;
        this.mqttConnOpts = mqttConnOpts;
        this.provider = provider;
        this.publisher = publisher;
        this.logger = logger;
        this.qos = qos;
        this.entityManager = entityManager;
    }

    void onPanelConnected() {
        if (!isPanelConnected) {
            isPanelConnected = true;
            publisher.publish("ABS/conn", "Status: Connected", qos, false, "initial connection");
            publisher.publish(HomeAssistantManager.AVAILABILITY_TOPIC, "online", qos, true, "online availability");
        }

        try {
            mqttClient.subscribe("ABS/+/set");
            mqttClient.subscribe("ABS/+/+/set");
            mqttClient.subscribe("homeassistant/status");
        } catch (Exception ex) {
            logger.warning("Error subscribing to topics: " + ex.getMessage());
        }
    }

    void onMqttConnectionLost(Throwable ex) {
        String msg = ex == null ? "" : ex.getMessage();
        logger.warning("Connection lost with the MQTT broker: " + msg + ". Reconnecting...");
        reconnectWithDelay("MQTT broker");
    }

    void onPanelConnectionLost() {
        logger.warning("Connection lost with the panel! Reconnecting...");
        reconnectWithDelay("panel");
    }

    private void reconnectWithDelay(String objName) {
        try {
            if (objName.equals("panel")) {
                isPanelConnected = false;
                ++panelReconnectionAttempts;
                logger.warning("Reconnection attempt " + panelReconnectionAttempts + " to " + objName + " in "
                        + RECON_DELAY_SECONDS + " seconds...");
                publisher.publish("ABS/conn", "Status: Disconnected", qos, false, "forced disconnection");
                publisher.publish(HomeAssistantManager.AVAILABILITY_TOPIC, "offline", qos, true, "offline availability");

                TimeUnit.SECONDS.sleep(RECON_DELAY_SECONDS);
                providerConnStatus status = provider.connect();
                if (status == providerConnStatus.UNREACHABLE) {
                    throw new Exception("Busy panel");
                } else if (status == providerConnStatus.SUCCESS) {
                    panelReconnectionAttempts = 0;
                    isPanelConnected = true;
                    publisher.publish("ABS/conn", "Status: Connected", qos, false, "successful reconnection");
                    publisher.publish(HomeAssistantManager.AVAILABILITY_TOPIC, "online", qos, true, "online availability");
                    entityManager.republishAllStates();
                }

                return;
            }

            if (objName.equals("MQTT broker")) {
                if (mqttReconnectAttempts >= MAX_MQTT_RECONNECT_ATTEMPTS) {
                    logger.severe(
                            "Maximum MQTT broker reconnection attempts reached. Restart the application.");
                    return;
                }

                ++mqttReconnectAttempts;
                logger.warning("Reconnection attempt " + mqttReconnectAttempts + "/" + MAX_MQTT_RECONNECT_ATTEMPTS
                        + " to the MQTT broker in " + RECON_DELAY_SECONDS + " seconds...");

                try {
                    if (mqttClient.isConnected()) {
                        mqttClient.disconnect();
                    }

                    TimeUnit.SECONDS.sleep(RECON_DELAY_SECONDS);
                    mqttClient.connect(mqttConnOpts);
                    mqttReconnectAttempts = 0;

                    // Resubscribe to topics after reconnection
                    mqttClient.subscribe("ABS/+/set");
                    mqttClient.subscribe("ABS/+/+/set");
                    mqttClient.subscribe("homeassistant/status");

                    logger.info("MQTT broker reconnection successful");
                } catch (Exception e) {
                    handleReconnectionFailure(objName, e);
                }

                return;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            handleReconnectionFailure(objName, ex);
        } catch (Exception ex) {
            handleReconnectionFailure(objName, ex);
        }
    }

    private void handleReconnectionFailure(String name, Exception ex) {
        logger.warning("Unable to reconnect to " + name + ". Cause: " + ex.getMessage());
        ex.printStackTrace();

        boolean shouldRetry = name.equals("panel")
                || (name.equals("MQTT broker") && mqttReconnectAttempts < MAX_MQTT_RECONNECT_ATTEMPTS);
        if (!shouldRetry) {
            return;
        }

        new Thread(() -> reconnectWithDelay(name)).start();
    }
}
