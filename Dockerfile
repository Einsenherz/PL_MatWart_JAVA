# Verwende ein offizielles Maven-JDK-Image
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Kopiere den Code und baue die App
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Führe die fertige JAR mit einer kleineren Runtime aus
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/target/myapp-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
