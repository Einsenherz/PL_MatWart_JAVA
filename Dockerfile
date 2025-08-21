# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Stabilere HTTP/HTTPS-Downloads für Maven (Retries/Timeouts)
ENV MAVEN_OPTS="\
 -Dmaven.wagon.http.retryHandler.count=5 \
 -Dmaven.wagon.http.pool=false \
 -Dmaven.wagon.rto=60000 \
 -Dhttp.keepAlive=false \
 -Dhttps.protocols=TLSv1.2,TLSv1.3 \
"

# 1) Nur pom.xml kopieren (ermöglicht besseren Docker-Layer-Cache)
COPY pom.xml .

# 2) Dependencies abrufen (mit Logs, kein -q; bei Netzproblemen siehst du den Grund)
RUN mvn -B -e -DskipTests dependency:resolve dependency:resolve-plugins

# 3) Quellen kopieren
COPY src ./src

# 4) Build mit Logs (kein -q), um Fehler im Render-Log wirklich zu sehen
RUN mvn -B -e -DskipTests clean package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Optionale JVM-Optimierungen für Container
ENV JAVA_TOOL_OPTIONS="\
 -XX:+UseContainerSupport \
 -XX:MaxRAMPercentage=75 \
 -Dfile.encoding=UTF-8 \
"

# Fat-JAR ins Runtime-Image
COPY --from=build /app/target/*.jar app.jar

# Standard-Port
EXPOSE 8080

# Startkommando
ENTRYPOINT ["java","-jar","/app/app.jar"]
