import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MqttMessageDispatcher {
    private static final Logger logger = Logger.getLogger(MqttMessageDispatcher.class.getName());
    private static final int MAX_PENDING_REQUESTS = 2048;
    private static final long RETRY_DELAY_MS = 1000;

    private final MqttClient mqttClient;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>(MAX_PENDING_REQUESTS);
    private final ConcurrentMap<String, PublishRequest> pendingByTopic = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Boolean> queuedTopics = new ConcurrentHashMap<>();
    private final Object queueLock = new Object();
    private final Thread worker;
    private volatile boolean running = true;

    public MqttMessageDispatcher(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
        this.worker = new Thread(this::processQueue, "MqttMessageDispatcher-Worker");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    public void stop() {
        running = false;
        worker.interrupt();
    }

    public void publishString(String topic, String payload, int qos, boolean retained) {
        if (!running || topic == null) {
            return;
        }

        MqttMessage msg = new MqttMessage(payload == null ? new byte[0] : payload.getBytes(StandardCharsets.UTF_8));
        msg.setQos(qos);
        msg.setRetained(retained);
        synchronized (queueLock) {
            pendingByTopic.put(topic, new PublishRequest(topic, msg));

            if (queuedTopics.putIfAbsent(topic, Boolean.TRUE) == null) {
                if (!queue.offer(topic)) {
                    queuedTopics.remove(topic);
                    pendingByTopic.remove(topic);
                    logger.warning("MQTT publish queue is full; dropped pending message for topic " + topic);
                }
            }
        }
    }

    private void processQueue() {
        while (running) {
            try {
                String topic = queue.poll(1, TimeUnit.SECONDS);
                if (topic == null) {
                    continue;
                }
                PublishRequest req;
                synchronized (queueLock) {
                    queuedTopics.remove(topic);
                    req = pendingByTopic.remove(topic);
                }
                if (req == null) {
                    continue;
                }

                try {
                    mqttClient.publish(
                        req.topic,
                        req.message.getPayload(),
                        req.message.getQos(),
                        req.message.isRetained()
                    );
                } catch (MqttException e) {
                    if (running) {
                        logger.fine("MQTT publish failed for topic " + topic + "; retrying: " + e.getMessage());
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        synchronized (queueLock) {
                            pendingByTopic.putIfAbsent(topic, req);
                            if (queuedTopics.putIfAbsent(topic, Boolean.TRUE) == null) {
                                if (!queue.offer(topic)) {
                                    queuedTopics.remove(topic);
                                    pendingByTopic.remove(topic);
                                    logger.warning("MQTT publish queue is full; dropped retry for topic " + topic);
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static class PublishRequest {
        final String topic;
        final MqttMessage message;
        PublishRequest(String topic, MqttMessage message) {
            this.topic = topic;
            this.message = message;
        }
    }
}
