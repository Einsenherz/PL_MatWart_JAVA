# --- Build Stage ---
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Systemtools + Maven installieren
RUN apk add --no-cache maven

# Nur pom.xml kopieren (ermöglicht Dependency-Caching)
COPY pom.xml ./

# Abhängigkeiten vorab laden
RUN mvn -B dependency:go-offline

# Jetzt den Source-Code kopieren
COPY src ./src

# Kompilieren + Jar erzeugen (Tests überspringen für schnelleren Build)
RUN mvn -B -DskipTests clean package spring-boot:repackage

# --- Runtime Stage ---
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Die fertige JAR aus dem Build übernehmen
COPY --from=build /app/target/myapp-0.0.1-SNAPSHOT.jar app.jar

# Port für Spring Boot
EXPOSE 8080

# Startkommando
ENTRYPOINT ["java","-jar","app.jar"]
