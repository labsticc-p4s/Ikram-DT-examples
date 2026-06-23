
FROM maven:3.9.6-eclipse-temurin-17 AS shared-build
WORKDIR /app

# Layer 1: install greycat SDK (cached unless jar changes)
COPY greycatSDK/greycat-sdk-7.8.1-stable.jar /tmp/greycat-sdk.jar
RUN mvn install:install-file -Dfile=/tmp/greycat-sdk.jar \
    -DgroupId=greycat -DartifactId=sdk -Dversion=7.8.1-stable -Dpackaging=jar

# Layer 2: build shared (cached unless shared/pom.xml or shared/src changes)
COPY shared/pom.xml shared/pom.xml
COPY shared/src     shared/src
RUN mvn install -f shared/pom.xml -DskipTests -q


FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app


COPY greycatSDK/greycat-sdk-7.8.1-stable.jar /tmp/greycat-sdk.jar
RUN mvn install:install-file -Dfile=/tmp/greycat-sdk.jar -DgroupId=greycat -DartifactId=sdk -Dversion=7.8.1-stable -Dpackaging=jar


COPY --from=shared-build /root/.m2/repository/com/bioreactorDT/shared \
     /root/.m2/repository/com/bioreactorDT/shared


COPY strain-service/pom.xml .
RUN mvn dependency:go-offline -B -q

COPY strain-service/src ./src
EXPOSE 8085
ENTRYPOINT ["mvn", "spring-boot:run", "-q"]