FROM adoptopenjdk:11-jdk-hotspot as build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Build all the dependencies in preparation to go offline.
# This is a seperate step so the dependencies will be cached unless
# the pom.xml file has changed
RUN ./mvnw dependency:go-offline -B

# Copy the project source
COPY src src

# Package the application
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

## Stage 2: A minimal docker image with command to run the app
FROM openjdk:11-jre-buster
ARG DEPENDENCY=/app/target/dependency

# Copy the project dependencies from the build stage
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.dailywords.collector.CollectorApplication"]