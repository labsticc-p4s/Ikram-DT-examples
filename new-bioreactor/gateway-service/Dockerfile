
FROM maven:3.9.6-eclipse-temurin-17 AS shared-build
WORKDIR /app

COPY greycatSDK/greycat-sdk-7.8.1-stable.jar /tmp/greycat-sdk.jar
RUN mvn install:install-file -Dfile=/tmp/greycat-sdk.jar \
    -DgroupId=greycat -DartifactId=sdk -Dversion=7.8.1-stable -Dpackaging=jar

COPY shared/pom.xml shared/pom.xml
COPY shared/src     shared/src
RUN mvn install -f shared/pom.xml -DskipTests -q



FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app


COPY greycatSDK/greycat-sdk-7.8.1-stable.jar /tmp/greycat-sdk.jar
RUN mvn install:install-file -Dfile=/tmp/greycat-sdk.jar -DgroupId=greycat -DartifactId=sdk -Dversion=7.8.1-stable -Dpackaging=jar


COPY --from=shared-build /root/.m2/repository/com/bioreactorDT/shared \
     /root/.m2/repository/com/bioreactorDT/shared


COPY gateway-service/pom.xml .
RUN mvn dependency:go-offline -B -q

COPY gateway-service/src ./src
EXPOSE 8086
ENTRYPOINT ["mvn", "spring-boot:run", "-q"]
