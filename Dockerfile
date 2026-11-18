# Use a Temurin 21 base image
FROM eclipse-temurin:21-jdk-jammy


# Create a directory for the application
WORKDIR /app

# Copy the JAR and dependencies
COPY build/app.jar /app/app.jar
COPY lib/jars/*.jar /app/lib/

# Environment variables (can be overridden at runtime)
ENV MQTT_ADDRESS="" \
    MQTT_PORT="" \
    MQTT_USERNAME="" \
    MQTT_PASSWORD="" \
    ALARM_ADDRESS="" \
    ALARM_PIN="" \
    ALARM_PORT=""

# Start command
ENTRYPOINT exec java -Duser.timezone=${TZ:-UTC} -cp /app/app.jar:/app/lib/* Application
