import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MqttMessageDispatcher {
    private final MqttClient mqttClient;
    private final BlockingQueue<PublishRequest> queue = new LinkedBlockingQueue<>();
    private final Thread worker;
    private volatile boolean running = true;

    public MqttMessageDispatcher(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
        this.worker = new Thread(this::processQueue, "MqttMessageDispatcher-Worker");
        this.worker.start();
    }

    public void stop() {
        running = false;
        worker.interrupt();
    }

    public void publish(String topic, MqttMessage message) {
        queue.offer(new PublishRequest(topic, message));
    }

    private void processQueue() {
        while (running) {
            try {
                PublishRequest req = queue.take();
                try {
                    mqttClient.publish(
                        req.topic,
                        req.message.getPayload(),
                        req.message.getQos(),
                        req.message.isRetained()
                    );
                    // Success: next message will be processed automatically
                } catch (MqttException e) {
                    // On failure, requeue the message for retry
                    queue.offer(req);
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            } catch (InterruptedException e) {
                // Thread interrupted, likely shutting down
                Thread.currentThread().interrupt();
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
