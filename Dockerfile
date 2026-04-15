FROM eclipse-temurin:21-jre-alpine
LABEL authors="egorm"

WORKDIR /app
COPY target/jwt-validator-service-0.0.1-SNAPSHOT.jar /app/jwtvalidator.jar
EXPOSE 9050
ENTRYPOINT ["java", "-jar", "jwtvalidator.jar"]