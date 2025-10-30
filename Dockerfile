FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven
RUN mvn -B -e -X -DskipTests clean package

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/myapp-0.0.1-SNAPSHOT.jar"]
