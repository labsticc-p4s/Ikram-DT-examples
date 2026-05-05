FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/bioreactor-mock-service-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/demo.csv /app/demo.csv
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]