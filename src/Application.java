import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import cms.device.api.Panel;
import cms.device.spi.PanelProvider;
import plugin.absoluta.AbsolutaPlugin;

public class Application {
   // Restituisce il valore della variabile d'ambiente o, se vuota/nulla, dal file di configurazione
   private static String getConfigValue(Properties props, String envKey, String propKey) {
      String value = System.getenv(envKey);
      if (value == null || value.isEmpty()) {
         value = props.getProperty(propKey);
      }
      return value;
   }

   public Application() {
   }

   public static void main(String[] var0) {
      Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream("config.properties")) {
         props.load(fis);
      } catch (IOException e) {
         System.err.println("Impossibile caricare config.properties: " + e.getMessage());
      }

      String MQTT_ADDRESS = getConfigValue(props, "MQTT_ADDRESS", "MQTT_ADDRESS");
      String MQTT_PORT = getConfigValue(props, "MQTT_PORT", "MQTT_PORT");
      String Username = getConfigValue(props, "MQTT_USERNAME", "MQTT_USERNAME");
      String Password = getConfigValue(props, "MQTT_PASSWORD", "MQTT_PASSWORD");
      String ADDRESS = getConfigValue(props, "ALARM_ADDRESS", "ALARM_ADDRESS");
      String PIN = getConfigValue(props, "ALARM_PIN", "ALARM_PIN");
      String PORT = getConfigValue(props, "ALARM_PORT", "ALARM_PORT");
      String MQTT_CONNECT_ATTEMPTS_STR = getConfigValue(props, "MQTT_CONNECT_ATTEMPTS", "MQTT_CONNECT_ATTEMPTS");
      String HOME_ASSISTANT_DISCOVERY = getConfigValue(props, "HOME_ASSISTANT_DISCOVERY", "HOME_ASSISTANT_DISCOVERY");

      MemoryPersistence memPers = new MemoryPersistence();

      int MQTT_CONNECT_ATTEMPTS = 5; // Default value
      if (MQTT_CONNECT_ATTEMPTS_STR != null) {
         try {
            MQTT_CONNECT_ATTEMPTS = Integer.parseInt(MQTT_CONNECT_ATTEMPTS_STR);
         } catch (NumberFormatException e) {
            System.err.println("MQTT_CONNECT_ATTEMPTS non è un numero valido, utilizzando il valore predefinito di 5");
         }
      } else {
         System.err.println("MQTT_CONNECT_ATTEMPTS non è definito, utilizzo il valore predefinito di 5");
      }

      boolean discoveryEnabled = true;
      if (HOME_ASSISTANT_DISCOVERY != null) {
         discoveryEnabled = HOME_ASSISTANT_DISCOVERY.equalsIgnoreCase("true");
      } else {
         System.err.println("HOME_ASSISTANT_DISCOVERY non è definito, utilizzo il valore predefinito di true");
      }

      for(int i = 0; i < MQTT_CONNECT_ATTEMPTS;  i++) {
         try {
            System.out.println("Tentativo di connessione numero: " + (i + 1));
            // Stampa i valori delle variabili d'ambiente per debug
            System.out.println("MQTT_ADDRESS=" + MQTT_ADDRESS);
            System.out.println("MQTT_PORT=" + MQTT_PORT);
            System.out.println("MQTT_USERNAME=" + Username);
            System.out.println("MQTT_PASSWORD=" + (Password != null ? "***" : "non definito"));
            System.out.println("ALARM_ADDRESS=" + ADDRESS);
            System.out.println("ALARM_PIN=" + PIN != null ? "***" : "non definito");
            System.out.println("ALARM_PORT=" + PORT);
            System.out.println("HOME_ASSISTANT_DISCOVERY=" + discoveryEnabled);
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
            PanelProvider provider = (new AbsolutaPlugin()).newPanel(map);
            Panel panel = new Panel(provider);
            Callback callback = new Callback(mqttClient, panel, mqttOption, discoveryEnabled);
            mqttClient.setCallback(callback);
            mqttClient.connect(mqttOption);
            System.out.println("Connesso");
            provider.initialize(callback);
            panel.connect();
         } catch (MqttException ex) {
            System.out.println("Exception: " + ex.getReasonCode());
            System.out.println("Attendo 15 secondi prima del prossimo tentativo...");
            try {
               Thread.sleep(15000L);
            } catch (InterruptedException e) {
               System.err.println("Interruzione durante l'attesa tra i tentativi di connessione: " + e.getMessage());
            }
         }
         i = MQTT_CONNECT_ATTEMPTS; // Forza l'uscita dal ciclo dopo il primo tentativo
      }

   }
}