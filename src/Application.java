import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import absoluta.AbsolutaPanelProvider;
import protocol.dsc.session.DscChannelInitializer;

import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

public class Application {

   private static final Logger logger = Logger.getLogger(Application.class.getName());
   // X.Y.Z Major.Minor.Patch
   // Major: Significant changes, breaking API
   // Minor: New features, backward compatibility
   // Patch: Bug fixes, minor improvements
   private static final String VERSION = "2.0.0";

   // Returns the environment variable value or, if empty/null, the value from the configuration file
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
            logger.severe("Error loading the configuration file: " + e.getMessage());
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
      String NETTY_LOG_LEVEL = getConfigValue(props, "NETTY_LOG_LEVEL");

      if (MQTT_ADDRESS == null || MQTT_PORT == null || Username == null || Password == null ||
         ADDRESS == null || PIN == null || PORT == null) {
         logger.severe("MQTT_ADDRESS, MQTT_PORT, MQTT_USERNAME, MQTT_PASSWORD, ALARM_ADDRESS, ALARM_PIN, and ALARM_PORT must be set!" +
            (configFromFile ? " Check the config.properties configuration file." : "No config.properties file found. Check the environment variables."));
         System.exit(1);
      }

      MemoryPersistence memPers = new MemoryPersistence();

      int MQTT_CONNECT_ATTEMPTS = parseIntOrDefault(MQTT_CONNECT_ATTEMPTS_STR, 5, logger, "MQTT_CONNECT_ATTEMPTS");
      boolean discoveryEnabled = HOME_ASSISTANT_DISCOVERY == null ? true : HOME_ASSISTANT_DISCOVERY.equalsIgnoreCase("true");

      Level logLevel = parseLogLevel(LOG_LEVEL);
      DscChannelInitializer.configureNettyLogging(NETTY_LOG_LEVEL);

      // Set the level on the root logger
      Logger rootLogger = Logger.getLogger("");
      rootLogger.setLevel(logLevel);

      // Remove all existing handlers from the root logger
      for (java.util.logging.Handler h : rootLogger.getHandlers()) {
         rootLogger.removeHandler(h);
      }

      // Add one configured ConsoleHandler
      java.util.logging.ConsoleHandler handler = new java.util.logging.ConsoleHandler();
      handler.setLevel(Level.ALL);
      rootLogger.addHandler(handler);

      // Disable detailed logs for external library packages
      Logger.getLogger("org.eclipse.paho.client.mqttv3").setLevel(Level.WARNING);
      Logger.getLogger("io.netty").setLevel(Level.WARNING);

      // If LOG_LOCATION is "FILE", set up a FileHandler
      if (LOG_LOCATION != null && LOG_LOCATION.equalsIgnoreCase("FILE")) {
         try {
            java.util.logging.FileHandler fileHandler =
               new java.util.logging.FileHandler("absoluta.log", 10 * 1024 * 1024, 1, false);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter()); // Use a simple text format
            rootLogger.addHandler(fileHandler);
         } catch (IOException e) {
            logger.severe("Error creating the FileHandler: " + e.getMessage());
         }
      } else {
         logger.info("Console logging enabled. To log to a file, set LOG_LOCATION to 'FILE'.");
      }

      logger.info("Starting Bentel Absoluta MQTT Bridge - Version " + VERSION);

      logger.fine("MQTT_ADDRESS=" + MQTT_ADDRESS);
      logger.fine("MQTT_PORT=" + MQTT_PORT);
      logger.fine("MQTT_USERNAME=" + Username);
      logger.fine("MQTT_PASSWORD=" + (Password != null ? "***" : "not set"));
      logger.fine("ALARM_ADDRESS=" + ADDRESS);
      logger.fine("ALARM_PIN=" + (PIN != null ? "***" : "not set"));
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
            logger.info("Connecting to broker: " + mqttServer);
            AbsolutaPanelProvider provider = new AbsolutaPanelProvider(ADDRESS, PIN, PORT);
            Callback callback = new Callback(mqttClient, provider, mqttOption, discoveryEnabled);
            mqttClient.setCallback(callback);
            mqttClient.connect(mqttOption);
            logger.info("Broker connected");
            provider.initialize(callback);

            int maxAttempts = 5;
            int attempt = 0;
            AbsolutaPanelProvider.providerConnStatus connStatus = AbsolutaPanelProvider.providerConnStatus.UNREACHABLE;
            while (attempt < maxAttempts && connStatus != AbsolutaPanelProvider.providerConnStatus.SUCCESS) {
               attempt++;
               connStatus = provider.connect();
               if (connStatus != AbsolutaPanelProvider.providerConnStatus.SUCCESS) {
                  logger.warning("Connection to the panel failed: " + connStatus + ". Retrying in 90 seconds...");
                  try {
                     Thread.sleep(90000L);
                     logger.info("Panel connection attempt #" + attempt);
                  } catch (InterruptedException e) {
                     logger.severe("Interrupted while waiting to reconnect to the panel: " + e.getMessage());
                     break;
                  }
               }
            }
            if (connStatus != AbsolutaPanelProvider.providerConnStatus.SUCCESS) {
               logger.severe("Unable to connect to the panel after " + maxAttempts + " attempts.");
               System.exit(1);
            }

            connected = true;
         } catch (MqttException ex) {
            logger.warning("Exception: " + ex.getReasonCode());
            logger.warning("Waiting 15 seconds before the next broker connection attempt...");
            try {
               Thread.sleep(15000L);
               logger.info("Broker connection attempt #" + (i + 1));
            } catch (InterruptedException e) {
               logger.severe("Interrupted while waiting between broker connection attempts: " + e.getMessage());
            }
         }
      }
      if (!connected) {
         logger.severe("Unable to connect to the MQTT broker after " + MQTT_CONNECT_ATTEMPTS + " attempts.");
         System.exit(1);
      }
   }

   // Helper for integer parsing with logging
   private static int parseIntOrDefault(String value, int defaultValue, Logger logger, String name) {
      if (value != null) {
         try {
            return Integer.parseInt(value);
         } catch (NumberFormatException e) {
            logger.warning(name + " is not a valid number; using default value " + defaultValue);
         }
      } else {
         logger.warning(name + " is not set; using default value " + defaultValue);
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