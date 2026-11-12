import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import absoluta.AbsolutaPanelProvider;

import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

public class Application {

   private static final Logger logger = Logger.getLogger(Application.class.getName());
   // X.Y.Z Major.Minor.Patch
   // Major: Cambiamenti significativi, API breaking
   // Minor: Nuove funzionalità, compatibilità con le versioni precedenti
   // Patch: Correzioni di bug, miglioramenti minori
   private static final String VERSION = "1.3.4";

   // Restituisce il valore della variabile d'ambiente o, se vuota/nulla, dal file di configurazione
   private static String getConfigValue(Properties props, String key) {
      String value = System.getenv(key);
      if (value == null || value.isEmpty()) {
         value = props.getProperty(key);
      }
      return value;
   }

   public Application() {
   }

   public static void main(String[] text) {

      String tz = System.getenv("TZ");
      if (tz != null && !tz.isEmpty()) {
         TimeZone.setDefault(TimeZone.getTimeZone(tz));
      }

      boolean configFromFile = true;
      Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream("config.properties")) {
         props.load(fis);
      } catch (IOException e) {
         if (!e.getMessage().contains("No such file or directory")) {
            logger.severe("Errore durante il caricamento del file di configurazione: " + e.getMessage());
            System.exit(1);
         }
         else {
            configFromFile = false;
         }
      }

      String MQTT_ADDRESS = getConfigValue(props, "MQTT_ADDRESS");
      String MQTT_PORT = getConfigValue(props, "MQTT_PORT");
      String Username = getConfigValue(props, "MQTT_USERNAME");
      String Password = getConfigValue(props, "MQTT_PASSWORD");
      String ADDRESS = getConfigValue(props, "ALARM_ADDRESS");
      String PIN = getConfigValue(props, "ALARM_PIN");
      String PORT = getConfigValue(props, "ALARM_PORT");
      String MQTT_CONNECT_ATTEMPTS_STR = getConfigValue(props, "MQTT_CONNECT_ATTEMPTS");
      String HOME_ASSISTANT_DISCOVERY = getConfigValue(props, "HOME_ASSISTANT_DISCOVERY");
      String LOG_LEVEL = getConfigValue(props, "LOG_LEVEL");
      String LOG_LOCATION = getConfigValue(props, "LOG_LOCATION");

      if (MQTT_ADDRESS == null || MQTT_PORT == null || Username == null || Password == null ||
         ADDRESS == null || PIN == null || PORT == null) {
         logger.severe("MQTT_ADDRESS, MQTT_PORT, MQTT_USERNAME, MQTT_PASSWORD, ALARM_ADDRESS, ALARM_PIN e ALARM_PORT devono essere valorizzati!" + 
            (configFromFile ? " Controlla il file di configurazione config.properties." : "Nessun file config.properties trovato. Controlla le variabili d'ambiente."));
         System.exit(1);
      }

      MemoryPersistence memPers = new MemoryPersistence();

      int MQTT_CONNECT_ATTEMPTS = parseIntOrDefault(MQTT_CONNECT_ATTEMPTS_STR, 5, logger, "MQTT_CONNECT_ATTEMPTS");
      boolean discoveryEnabled = HOME_ASSISTANT_DISCOVERY == null ? true : HOME_ASSISTANT_DISCOVERY.equalsIgnoreCase("true");

      Level logLevel = parseLogLevel(LOG_LEVEL);

      // Imposta il livello sul root logger
      Logger rootLogger = Logger.getLogger("");
      rootLogger.setLevel(logLevel);

      // Rimuovi tutti gli handler esistenti dal root logger
      for (java.util.logging.Handler h : rootLogger.getHandlers()) {
         rootLogger.removeHandler(h);
      }

      // Aggiungi un solo ConsoleHandler configurato al root logger
      java.util.logging.ConsoleHandler handler = new java.util.logging.ConsoleHandler();
      handler.setLevel(logLevel);
      rootLogger.addHandler(handler);

      // Disabilita log dettagliati per i package di librerie esterne
      Logger.getLogger("org.eclipse.paho.client.mqttv3").setLevel(Level.WARNING);
      Logger.getLogger("io.netty").setLevel(Level.WARNING);

      // Se LOG_LOCATION è "FILE", imposta un FileHandler
      if (LOG_LOCATION != null && LOG_LOCATION.equalsIgnoreCase("FILE")) {
         try {
            java.util.logging.FileHandler fileHandler = new java.util.logging.FileHandler("absoluta.log", false);
            fileHandler.setLevel(logLevel);
            fileHandler.setFormatter(new SimpleFormatter()); // usa un formato di testo semplice
            rootLogger.addHandler(fileHandler);
         } catch (IOException e) {
            logger.severe("Errore durante la creazione del FileHandler: " + e.getMessage());
         }
      } else {
         logger.info("Registrazione su console abilitata. Per registrare su file, imposta LOG_LOCATION a 'FILE'.");
      }

      logger.info("Avvio Bentel Absoluta MQTT Bridge - Versione " + VERSION);

      logger.fine("MQTT_ADDRESS=" + MQTT_ADDRESS);
      logger.fine("MQTT_PORT=" + MQTT_PORT);
      logger.fine("MQTT_USERNAME=" + Username);
      logger.fine("MQTT_PASSWORD=" + (Password != null ? "***" : "non definito"));
      logger.fine("ALARM_ADDRESS=" + ADDRESS);
      logger.fine("ALARM_PIN=" + (PIN != null ? "***" : "non definito"));
      logger.fine("ALARM_PORT=" + PORT);
      logger.fine("HOME_ASSISTANT_DISCOVERY=" + discoveryEnabled);

      boolean connected = false;
      for (int i = 0; i < MQTT_CONNECT_ATTEMPTS && !connected; i++) {
         try {
            String mqttServer = "tcp://" + MQTT_ADDRESS + ":" + MQTT_PORT;
            MqttClient mqttClient = new MqttClient(mqttServer, "absolutamqtt", memPers);
            MqttConnectOptions mqttOption = new MqttConnectOptions();
            mqttOption.setCleanSession(true);
            mqttOption.setUserName(Username);
            mqttOption.setPassword(Password.toCharArray());
            mqttOption.setAutomaticReconnect(true);
            mqttOption.setKeepAliveInterval(60);  // 60 seconds keep-alive
            mqttOption.setConnectionTimeout(30);   // 30 seconds connection timeout
            mqttOption.setWill("ABS/availability", "offline".getBytes(), 1, true);
            logger.info("Collegamento al broker: " + mqttServer);
            AbsolutaPanelProvider provider = new AbsolutaPanelProvider(ADDRESS, PIN, PORT);
            Callback callback = new Callback(mqttClient, provider, mqttOption, discoveryEnabled);
            mqttClient.setCallback(callback);
            mqttClient.connect(mqttOption);
            logger.info("Broker connesso");
            provider.initialize(callback);

            int maxAttempts = 5;
            int attempt = 0;
            AbsolutaPanelProvider.providerConnStatus connStatus = AbsolutaPanelProvider.providerConnStatus.UNREACHABLE;
            while (attempt < maxAttempts && connStatus != AbsolutaPanelProvider.providerConnStatus.SUCCESS) {
               attempt++;
               connStatus = provider.connect();
               if (connStatus != AbsolutaPanelProvider.providerConnStatus.SUCCESS) {
                  logger.warning("Connessione con la centrale fallita: " + connStatus + ". Riprovo tra 90 secondi...");
                  try {
                     Thread.sleep(90000L);
                     logger.info("Tentativo di connessione alla centrale n° " + attempt);
                  } catch (InterruptedException e) {
                     logger.severe("Interrotto durante l'attesa di ricollegamento alla centrale: " + e.getMessage());
                     break;
                  }
               }
            }
            if (connStatus != AbsolutaPanelProvider.providerConnStatus.SUCCESS) {
               logger.severe("Impossibile connettersi alla centrale dopo " + maxAttempts + " tentativi.");
               System.exit(1);
            }

            // Avvio il thread di ping TCP solo dopo la connessione alla centrale
            logger.info("Avvio thread di ping TCP verso " + ADDRESS + ":" + PORT);
            PingKeepAlive pingKeepAlive = new PingKeepAlive(ADDRESS, Integer.parseInt(PORT));
            pingKeepAlive.start();

            connected = true;
         } catch (MqttException ex) {
            logger.warning("Exception: " + ex.getReasonCode());
            logger.warning("Attendo 15 secondi prima del prossimo tentativo per il collegamento al broker...");
            try {
               Thread.sleep(15000L);
               logger.info("Tentativo di connessione al broker numero: " + (i + 1));
            } catch (InterruptedException e) {
               logger.severe("Interruzione durante l'attesa tra i tentativi di connessione al broker: " + e.getMessage());
            }
         }
      }
      if (!connected) {
         logger.severe("Impossibile connettersi al broker MQTT dopo " + MQTT_CONNECT_ATTEMPTS + " tentativi.");
         System.exit(1);
      }
   }

   // Helper per parsing int con log
   private static int parseIntOrDefault(String value, int defaultValue, Logger logger, String name) {
      if (value != null) {
         try {
            return Integer.parseInt(value);
         } catch (NumberFormatException e) {
            logger.warning(name + " non è un numero valido, utilizzando il valore predefinito di " + defaultValue);
         }
      } else {
         logger.warning(name + " non è definito, utilizzo il valore predefinito di " + defaultValue);
      }
      return defaultValue;
   }

   private static Level parseLogLevel(String level) {
      if (level == null || level.isEmpty()) {
         return Level.WARNING;
      }
      switch (level.toUpperCase()) {
         case "SEVERE":
            return Level.SEVERE;
         case "WARNING":
            return Level.WARNING;
         case "INFO":
            return Level.INFO;
         case "CONFIG":
            return Level.CONFIG;
         case "FINE":
            return Level.FINE;
         case "FINER":
            return Level.FINER;
         case "FINEST":
            return Level.FINEST;
         default:
            return Level.WARNING;
      }
   }
}