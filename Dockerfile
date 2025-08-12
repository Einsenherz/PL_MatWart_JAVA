# ---------- Build stage ----------
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Maven Wrapper optional – wenn du keinen wrapper hast, nimm das Standard-Maven-Image raus.
# Falls du den Wrapper benutzt, kopiere ihn mit:
# COPY mvnw pom.xml ./
# COPY .mvn .mvn
# RUN chmod +x mvnw && ./mvnw -v

# Ohne Wrapper: Maven über das Image installieren
RUN apk add --no-cache maven

# Erst pom kopieren und Dependencies cachen
COPY pom.xml ./pom.xml
RUN mvn -q -e -B dependency:go-offline

# Dann den Rest
COPY src ./src

# Bauen (Tests überspringen, optional)
RUN mvn -q -e -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine

# Für H2-Datei-DB braucht das Image Schreibrechte
WORKDIR /app

# Jar vom Build übernehmen
COPY --from=build /app/target/*.jar /app/app.jar

# Port
EXPOSE 8080

# Start
ENTRYPOINT ["java","-jar","/app/app.jar"]
