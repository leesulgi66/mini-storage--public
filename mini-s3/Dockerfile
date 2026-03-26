FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
VOLUME /app/data
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]