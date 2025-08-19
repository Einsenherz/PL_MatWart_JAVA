# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Setze Maven auf Single-Thread Build (stabiler in Render/Docker)
ENV MAVEN_OPTS="-Dmaven.artifact.threads=1"

# Erst nur pom.xml kopieren und Dependencies cachen
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Danach Source-Code kopieren
COPY src ./src

# Build (Tests überspringen, um schneller zu deployen)
RUN mvn clean package -DskipTests -B

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Fat Jar ins Runtime-Image kopieren
COPY --from=build /app/target/*.jar app.jar

# Falls H2 oder andere Dateien persistent sein müssen, Mountpoint
VOLUME /app/data

# Port, den Spring Boot bindet
EXPOSE 8080

# Startkommando
ENTRYPOINT ["java","-jar","/app/app.jar"]
