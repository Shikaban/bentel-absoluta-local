import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

final class Entities {
   final Map<Integer, Partition> partitionsById = new HashMap<>();
   final Map<String, Integer> partitionIdByTopic = new HashMap<>();

   final Map<Integer, Sensor> sensorsById = new HashMap<>();
   final Map<String, Integer> sensorIdByTopic = new HashMap<>();

   final String[] modeNames = new String[4];

   final HashSet<Integer> sensorDiscoverySent = new HashSet<>();
   final HashSet<Integer> partitionDiscoverySent = new HashSet<>();
   final Map<Integer, String> partitionReadinessDiscoveryNames = new HashMap<>();
   final HashSet<Integer> modeDiscoverySent = new HashSet<>();

   List<String> panelFaults = Collections.emptyList();
   List<String> alarmMemory = Collections.emptyList();

   static final class Partition {
      String name;
      String topic;
      String arming;
      String status;
      String readiness = "unknown";
   }

   static final class Sensor {
      String name;
      String topic;
      String status;
      String bypass;
   }
}
