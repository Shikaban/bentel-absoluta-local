# Usa una immagine base temurin 21
FROM eclipse-temurin:21-jdk-jammy


# Crea una directory per l'app
WORKDIR /app

# Copia il JAR e le dipendenze
COPY build/app.jar /app/app.jar
COPY lib/jars/*.jar /app/lib/

# Variabili d'ambiente (possono essere sovrascritte in fase di run)
ENV MQTT_ADDRESS="" \
    MQTT_PORT="" \
    MQTT_USERNAME="" \
    MQTT_PASSWORD="" \
    ALARM_ADDRESS="" \
    ALARM_PIN="" \
    ALARM_PORT=""

# Comando di avvio
ENTRYPOINT exec java -Duser.timezone=${TZ:-UTC} -cp /app/app.jar:/app/lib/* Application
