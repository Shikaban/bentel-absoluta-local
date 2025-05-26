import java.util.HashMap;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import cms.device.api.Panel;
import cms.device.spi.PanelProvider;
import plugin.absoluta.AbsolutaPlugin;

public class Application {
   private static final String MQTT_SERVER = "tcp://MQTT-IP:MQTT-PORT";
   private static final String PIN = "YOUR_PIN";
   private static final String PORT = "3064";
   private static final String ADDRESS = "YOUR_IP";

   public Application() {
   }

   public static void main(String[] var0) {
      MemoryPersistence memPers = new MemoryPersistence();

      try {
         MqttClient mqttClient = new MqttClient(MQTT_SERVER, "absolutamqtt", memPers);
         MqttConnectOptions mqttOption = new MqttConnectOptions();
         mqttOption.setCleanSession(true);
         System.out.println("Collegamento al broker: " + MQTT_SERVER);
         HashMap map = new HashMap();
         map.put("pin", PIN);
         map.put("port", PORT);
         map.put("address", ADDRESS);
         map.put("type", "absoluta");
         PanelProvider provider = (new AbsolutaPlugin()).newPanel(map);
         Panel panel = new Panel(provider, "absoluta");
         Callback callback = new Callback(mqttClient, panel, mqttOption);
         mqttClient.setCallback(callback);
         mqttClient.connect(mqttOption);
         System.out.println("Connesso");
         provider.initialize(callback);
         panel.connect();
      } catch (MqttException ex) {
         System.out.println("Exception: " + ex.getReasonCode());
      }
   }
}