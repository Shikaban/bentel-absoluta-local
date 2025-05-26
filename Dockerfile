# Usa una immagine base OpenJDK 24
FROM openjdk:24-slim

# Crea una directory per l'app
WORKDIR /app

# Copia il JAR e le dipendenze
COPY build/app.jar /app/app.jar
COPY lib/jars/*.jar /app/lib/
COPY secured/*.jar /app/lib/

# Variabili d'ambiente (possono essere sovrascritte in fase di run)
ENV MQTT_ADDRESS="" \
    MQTT_PORT="" \
    MQTT_USERNAME="" \
    MQTT_PASSWORD="" \
    ALARM_ADDRESS="" \
    ALARM_PIN="" \
    ALARM_PORT=""

# Comando di avvio
ENTRYPOINT ["java", "-cp", "/app/app.jar:/app/lib/*", "Application"]
