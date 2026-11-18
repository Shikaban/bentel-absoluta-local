# Configuration

All parameters are passed as environment variables in `docker-compose.yml`.

## Environment Variables

| Variable                   | Required | Default   | Example        | Notes |
|----------------------------|----------|-----------|----------------|-------|
| `MQTT_ADDRESS`             | Yes      | —         | `192.168.1.10` | Hostname or IP of the MQTT broker |
| `MQTT_PORT`                | No       | `1883`    | `1883`         | |
| `MQTT_USERNAME`            | No       | *(empty)* | `absoluta`     | Leave empty only if the broker has no authentication. See security note below. |
| `MQTT_PASSWORD`            | No       | *(empty)* | `yourpassword` | Leave empty only if the broker has no authentication. See security note below. |
| `ALARM_ADDRESS`            | Yes      | —         | `192.168.1.20` | Hostname or IP of the Bentel Absoluta panel |
| `ALARM_PORT`               | No       | `3064`    | `3064`         | |
| `ALARM_PIN`                | Yes      | —         | `123456`       | Panel user PIN (1–6 digits). See security note below. |
| `MQTT_CONNECT_ATTEMPTS`    | No       | `5`       | `5`            | Number of initial MQTT connection attempts before the bridge gives up |
| `HOME_ASSISTANT_DISCOVERY` | No       | `true`    | `true`         | Set to `false` to disable Home Assistant MQTT Discovery |
| `LOG_LEVEL`                | No       | `WARNING` | `FINER`        | Verbosity: `SEVERE` `WARNING` `INFO` `CONFIG` `FINE` `FINER` `FINEST`. For troubleshooting, `FINE` is usually sufficient. **Do not use `FINER` or `FINEST` in production: from `FINER` onwards, the panel PIN is written in plaintext in the logs.** |
| `LOG_LOCATION`             | No       | *(empty)* | `FILE`         | Set to `FILE` to write logs to a file; omit or leave empty for console output. When set to `FILE`, logs are written to `/app/absoluta.log` inside the container. |
| `NETTY_LOG_LEVEL`          | No       | `OFF`     | `TRACE`        | Netty wire/protocol tracing. Supported values: `OFF`, `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`. Leave `OFF` in normal use. |

---

## Security Notes

> **MQTT authentication:** Running a broker without credentials (`MQTT_USERNAME` / `MQTT_PASSWORD` left empty) is strongly discouraged. Anyone on your network could read alarm states or send arm/disarm commands. Always configure authentication on the broker.

> **Panel PIN:** It is strongly recommended to create a dedicated user on the Absoluta panel with its own PIN specifically for this bridge, rather than reusing an existing user's PIN. This limits the scope of the credentials and allows you to revoke access independently.

---

## Full docker-compose.yml Example

```yaml
version: "3.8"
services:
  bentel-absoluta:
    image: "axtx2301/bentel-absoluta-local-bridge:latest"
    restart: unless-stopped
    environment:
      MQTT_ADDRESS: "192.168.1.10"
      MQTT_PORT: "1883"
      MQTT_USERNAME: "your_mqtt_user"
      MQTT_PASSWORD: "your_mqtt_password"
      ALARM_ADDRESS: "192.168.1.20"
      ALARM_PORT: "3064"
      ALARM_PIN: "123456"
      MQTT_CONNECT_ATTEMPTS: "5"
      HOME_ASSISTANT_DISCOVERY: "true"
      LOG_LEVEL: "WARNING"
      LOG_LOCATION: ""
      NETTY_LOG_LEVEL: "OFF"
```
