FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
EXPOSE 8085
ENTRYPOINT ["mvn", "spring-boot:run", "-q"]