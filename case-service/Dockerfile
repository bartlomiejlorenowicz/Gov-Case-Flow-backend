FROM eclipse-temurin:21-jdk

WORKDIR /app

# potrzebne do healthcheck w docker-compose
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY target/case-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
