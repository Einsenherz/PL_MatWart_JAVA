# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Stabilere Downloads & ausführliche Logs
ENV MAVEN_OPTS="\
 -Dmaven.wagon.http.retryHandler.count=5 \
 -Dmaven.wagon.http.pool=false \
 -Dmaven.wagon.rto=60000 \
 -Dhttp.keepAlive=false \
 -Dhttps.protocols=TLSv1.2,TLSv1.3 \
"

COPY pom.xml .
RUN mvn -B -e -V dependency:resolve dependency:resolve-plugins

COPY src ./src

# 1) Nur kompilieren -> echte Compiler-Fehlerzeile sehen
RUN mvn -B -e -X -DskipTests clean compile

# 2) Wenn compile ok ist: Paket bauen
RUN mvn -B -e -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
