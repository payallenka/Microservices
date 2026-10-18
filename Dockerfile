# One multi-stage Dockerfile shared by every service; docker-compose passes SERVICE=<module directory>.
# The jar is built inside the image, so `docker compose up --build` never ships a stale local build.

FROM maven:3.9-eclipse-temurin-17 AS build
ARG SERVICE
ARG MAVEN_PROFILES=""
WORKDIR /build

# Resolve dependencies in their own layer so code changes don't re-download them.
COPY ${SERVICE}/pom.xml .
RUN mvn -B -q dependency:go-offline ${MAVEN_PROFILES:+-P$MAVEN_PROFILES}

COPY ${SERVICE}/src ./src
RUN mvn -B -q package -DskipTests ${MAVEN_PROFILES:+-P$MAVEN_PROFILES}


FROM eclipse-temurin:17-jre
RUN useradd --system --uid 1001 app
WORKDIR /app
COPY --from=build /build/target/app.jar app.jar
USER app

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
