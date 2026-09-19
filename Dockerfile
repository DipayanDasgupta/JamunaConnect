# Minimal runtime image: build with `mvn package`, then copy the jar in.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/jamunaconnect-*.jar app.jar
EXPOSE 8080
# Secrets and DB config are injected as environment variables, never baked in.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]