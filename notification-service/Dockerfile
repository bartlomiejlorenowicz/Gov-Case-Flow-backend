FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# root pom
COPY pom.xml .

COPY case-service/pom.xml case-service/pom.xml
COPY case-service/src case-service/src

COPY audit-service/pom.xml audit-service/pom.xml
COPY audit-service/src audit-service/src

COPY notification-service/pom.xml notification-service/pom.xml
COPY notification-service/src notification-service/src

RUN mvn -pl notification-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
