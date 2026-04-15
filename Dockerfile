FROM eclipse-temurin:21-jre-alpine
LABEL authors="egorm"

WORKDIR /app
COPY target/Stroy1Click-JwtValidatorService-0.0.1-SNAPSHOT.jar /app/jwtvalidator.jar
EXPOSE 9050
ENTRYPOINT ["java", "-jar", "jwtvalidator.jar"]