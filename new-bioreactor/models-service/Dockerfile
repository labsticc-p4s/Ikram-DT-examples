FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app

COPY greycatSDK/greycat-sdk-7.8.1-stable.jar /tmp/greycat-sdk.jar
RUN mvn install:install-file -Dfile=/tmp/greycat-sdk.jar -DgroupId=greycat -DartifactId=sdk -Dversion=7.8.1-stable -Dpackaging=jar

COPY models-service/pom.xml .
RUN mvn dependency:go-offline -B -q

COPY models-service/src ./src
EXPOSE 8084
ENTRYPOINT ["mvn", "spring-boot:run", "-q"]
