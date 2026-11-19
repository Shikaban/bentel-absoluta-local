import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttClient;

import absoluta.AbsolutaPanelProvider;
import absoluta.AbsolutaPanelProvider.providerConnStatus;

final class ConnectionManager {
    private static final int RECON_DELAY_SECONDS = 90;

    private final MqttClient mqttClient;
    private final AbsolutaPanelProvider provider;
    private final Publisher publisher;
    private final Logger logger;
    private final int qos;
    private final EntityManager entityManager;

    private int panelReconnectionAttempts = 0;
    private boolean isPanelConnected = false;
    private final AtomicBoolean panelReconnectScheduled = new AtomicBoolean();
    private final ScheduledExecutorService reconnectExecutor =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "Connection-Reconnect");
                thread.setDaemon(true);
                return thread;
            });

    ConnectionManager(MqttClient mqttClient, AbsolutaPanelProvider provider,
            Publisher publisher, Logger logger, int qos, EntityManager entityManager) {
        this.mqttClient = mqttClient;
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
        logger.warning("Connection lost with the MQTT broker: " + msg
                + ". Paho automatic reconnection is active.");
    }

    void onMqttConnected(boolean reconnect) {
        logger.info(reconnect ? "MQTT broker reconnection successful" : "MQTT broker connected");
        try {
            mqttClient.subscribe("ABS/+/set");
            mqttClient.subscribe("ABS/+/+/set");
            mqttClient.subscribe("homeassistant/status");
        } catch (Exception ex) {
            logger.warning("Error resubscribing after MQTT connection: " + ex.getMessage());
        }
    }

    void onPanelConnectionLost() {
        logger.warning("Connection lost with the panel! Reconnecting...");
        isPanelConnected = false;
        publisher.publish("ABS/conn", "Status: Disconnected", qos, false, "forced disconnection");
        publisher.publish(HomeAssistantManager.AVAILABILITY_TOPIC, "offline", qos, true, "offline availability");
        schedulePanelReconnect();
    }

    private void schedulePanelReconnect() {
        if (!panelReconnectScheduled.compareAndSet(false, true)) {
            return;
        }

        reconnectExecutor.schedule(this::reconnectPanel, RECON_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void reconnectPanel() {
        ++panelReconnectionAttempts;
        logger.warning("Panel reconnection attempt " + panelReconnectionAttempts
                + " after " + RECON_DELAY_SECONDS + " seconds...");

        try {
            providerConnStatus status = provider.connect();
            if (status == providerConnStatus.SUCCESS) {
                panelReconnectionAttempts = 0;
                isPanelConnected = true;
                panelReconnectScheduled.set(false);
                publisher.publish("ABS/conn", "Status: Connected", qos, false, "successful reconnection");
                publisher.publish(HomeAssistantManager.AVAILABILITY_TOPIC, "online", qos, true,
                        "online availability");
                entityManager.republishAllStates();
                return;
            }

            logger.warning("Connection to the panel failed: " + status
                    + ". Next attempt in " + RECON_DELAY_SECONDS + " seconds.");
            reconnectExecutor.schedule(this::reconnectPanel, RECON_DELAY_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.warning("Unable to reconnect to the panel: " + ex.getMessage());
            reconnectExecutor.schedule(this::reconnectPanel, RECON_DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }
}
