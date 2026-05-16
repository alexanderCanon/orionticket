FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S orion && adduser -S orion -G orion

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

USER orion

EXPOSE 8089

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8089/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-local}", \
    "-jar", "app.jar"]
