FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Venugopal R "
      name="Car Rental Service"
      description="A microservice for managing car rentals"
      version="1.0.0"
WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

USER 352

ENTRYPOINT ["java", "-jar", "app.jar"]