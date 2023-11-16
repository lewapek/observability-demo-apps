FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.8.1_1_1.9.6_3.3.1 AS build
WORKDIR /app
COPY . /app
RUN sbt assembly

FROM eclipse-temurin:17-jre-alpine
ARG APP_VERSION
WORKDIR /app
USER 1001
COPY --from=build --chown=1001 /app/target/app-assembly-${APP_VERSION}.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD []