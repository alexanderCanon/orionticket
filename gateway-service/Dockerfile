FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml .
RUN apk add --no-cache maven
RUN mvn dependency:go-offline -q
COPY src src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S orion && adduser -S orion -G orion
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
USER orion
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
