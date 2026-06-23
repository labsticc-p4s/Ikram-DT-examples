FROM maven:3.9-eclipse-temurin-17 AS shared-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn install -DskipTests -q