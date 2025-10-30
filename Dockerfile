# --- Build Stage ---
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Maven installieren
RUN apk add --no-cache maven

# Nur pom.xml zuerst kopieren (Dependency-Caching)
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Jetzt den Source-Code kopieren
COPY src ./src

# Kompilieren + Jar erzeugen (Tests überspringen für schnelleren Build)
RUN mvn -B -DskipTests clean package spring-boot:repackage

# --- Runtime Stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Die fertige JAR aus dem Build übernehmen
COPY --from=build /app/target/myapp-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
