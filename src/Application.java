import java.util.HashMap;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import cms.device.api.Panel;
import cms.device.spi.PanelProvider;
import plugin.absoluta.AbsolutaPlugin;

public class Application {
   private static final String MQTT_ADDRESS = System.getenv("MQTT_ADDRESS");
   private static final String MQTT_PORT = System.getenv("MQTT_PORT");
   private static final String Username = System.getenv("MQTT_USERNAME");
   private static final String Password = System.getenv("MQTT_PASSWORD");
   private static final String ADDRESS = System.getenv("ALARM_ADDRESS");
   private static final String PIN = System.getenv("ALARM_PIN");
   private static final String PORT = System.getenv("ALARM_PORT");

   public Application() {
   }

   public static void main(String[] var0) {
      MemoryPersistence memPers = new MemoryPersistence();

      try {
         // Stampa i valori delle variabili d'ambiente per debug
         System.out.println("MQTT_ADDRESS=" + MQTT_ADDRESS);
         System.out.println("MQTT_PORT=" + MQTT_PORT);
         System.out.println("MQTT_USERNAME=" + Username);
         System.out.println("MQTT_PASSWORD=" + (Password != null ? "***" : null));
         System.out.println("ALARM_ADDRESS=" + ADDRESS);
         System.out.println("ALARM_PIN=" + PIN);
         System.out.println("ALARM_PORT=" + PORT);
         // Controllo variabili obbligatorie
         if (MQTT_ADDRESS == null || MQTT_PORT == null || Username == null || Password == null) {
            throw new IllegalArgumentException("MQTT_ADDRESS, MQTT_PORT, MQTT_USERNAME e MQTT_PASSWORD devono essere valorizzati!");
         }
         String mqttServer = "tcp://" + MQTT_ADDRESS + ":" + MQTT_PORT;
         MqttClient mqttClient = new MqttClient(mqttServer, "absolutamqtt", memPers);
         MqttConnectOptions mqttOption = new MqttConnectOptions();
         mqttOption.setCleanSession(true);
         mqttOption.setUserName(Username);
         mqttOption.setPassword(Password.toCharArray());
         System.out.println("Collegamento al broker: " + mqttServer);
         HashMap<String, String> map = new HashMap<>();
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