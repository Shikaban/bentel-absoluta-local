public class HomeAssistantManager {

    public static final String AVAILABILITY_TOPIC = "ABS/availability";

    private static final String DEVICE_JSON =
        "\"device\": {" +
            "\"identifiers\": [\"absoluta_panel\"]," +
            "\"name\": \"Absoluta Panel\"," +
            "\"manufacturer\": \"Bentel\"," +
            "\"model\": \"Absoluta\"" +
        "}";

    private static final String AVAILABILITY_JSON =
        "\"availability_topic\": \"" + AVAILABILITY_TOPIC + "\"," +
        "\"payload_available\": \"online\"," +
        "\"payload_not_available\": \"offline\",";

    public static String buildPartition(int partitionID, String partitionName) {
        return "{" +
            "\"name\": \"" + partitionName + "\"," +
            "\"state_topic\": \"ABS/partition/" + partitionID + "\"," +
            "\"unique_id\": \"absoluta_partition_" + partitionID + "\"," +
            "\"command_topic\": \"ABS/partition/" + partitionID + "/set\"," +
            "\"code_arm_required\": false," +
            "\"code_disarm_required\": false," +
            "\"supported_features\": [\"arm_away\"]," +
            "\"payload_arm_away\": \"ARM_AWAY\"," +
            "\"payload_disarm\": \"DISARM\"," +
            AVAILABILITY_JSON +
            DEVICE_JSON +
        "}";
    }

    public static String buildPartitionReadiness(int partitionID, String partitionName) {
        return "{" +
            "\"name\": \"" + partitionName + " Readiness\"," +
            "\"state_topic\": \"ABS/partition/" + partitionID + "/readiness\"," +
            "\"unique_id\": \"absoluta_partition_" + partitionID + "_readiness\"," +
            "\"icon\": \"mdi:shield-check\"," +
            AVAILABILITY_JSON +
            DEVICE_JSON +
        "}";
    }

    public static String buildSensor(int sensorID, String sensorName) {
        return "{" +
            "\"name\": \"" + sensorName + "\"," +
            "\"state_topic\": \"ABS/sensor/" + sensorID + "\"," +
            "\"unique_id\": \"absoluta_sensor_" + sensorID + "\"," +
            "\"device_class\": \"motion\"," +
            AVAILABILITY_JSON +
            DEVICE_JSON +
        "}";
    }

    public static String buildSensorBypass(int sensorID, String sensorName) {
        return "{" +
            "\"name\": \"" + sensorName + " Bypass\"," +
            "\"state_topic\": \"ABS/sensor/" + sensorID + "_bypass\"," +
            "\"unique_id\": \"absoluta_sensor_" + sensorID + "_bypass\"," +
            "\"command_topic\": \"ABS/sensor/" + sensorID + "/set\"," +
            "\"payload_on\": \"ON\"," +
            "\"payload_off\": \"OFF\"," +
            "\"device_class\": \"switch\"," +
            AVAILABILITY_JSON +
            DEVICE_JSON +
        "}";
    }

    public static String buildMode(char modeChar, String modeLabel) {
        return "{" +
            "\"name\": \"" + modeLabel + "\"," +
            "\"state_topic\": \"ABS/mode/" + modeChar + "\"," +
            "\"unique_id\": \"absoluta_mode_" + modeChar + "\"," +
            "\"command_topic\": \"ABS/mode/" + modeChar + "/set\"," +
            "\"payload_press\": \"MODE_" + modeChar + "\"," +
            AVAILABILITY_JSON +
            DEVICE_JSON +
        "}";
    }

    public static String buildResetAlarmMemory() {
        return "{" +
            "\"name\": \"Reset Alarm Memory\"," +
            "\"state_topic\": \"ABS/reset_alarm_memory\"," +
            "\"unique_id\": \"absoluta_reset_alarm_memory\"," +
            "\"command_topic\": \"ABS/absoluta_alarm_memory/set\"," +
            "\"payload_press\": \"RESET_ALARM_MEMORY\"," +
            AVAILABILITY_JSON +
            DEVICE_JSON +
        "}";
    }

    public static String buildResetErrors() {
        return "{" +
            "\"name\": \"Reset Bridge Alerts\"," +
            "\"state_topic\": \"ABS/reset_bridge_alerts\"," +
            "\"unique_id\": \"absoluta_reset_bridge_alerts\"," +
            "\"command_topic\": \"ABS/absoluta_bridge_alerts/set\"," +
            "\"payload_press\": \"RESET_ERRORS\"," +
            AVAILABILITY_JSON +
            DEVICE_JSON +
        "}";
    }

    public static String buildTroublesSensor() {
        return "{" +
            "\"name\": \"Panel Faults\"," +
            "\"state_topic\": \"ABS/panel_faults\"," +
            "\"unique_id\": \"absoluta_panel_faults\"," +
            "\"icon\": \"mdi:alert-circle\"," +
            "\"json_attributes_topic\": \"ABS/panel_faults/attributes\"," +
            AVAILABILITY_JSON +
            DEVICE_JSON +
        "}";
    }

    public static String buildErrorSensor() {
        return "{" +
            "\"name\": \"Bridge Alerts\"," +
            "\"state_topic\": \"ABS/bridge_alerts\"," +
            "\"icon\": \"mdi:alert\"," +
            "\"unique_id\": \"absoluta_bridge_alerts\"," +
            "\"json_attributes_topic\": \"ABS/bridge_alerts/attributes\"," +
            AVAILABILITY_JSON +
            DEVICE_JSON +
        "}";
    }
}