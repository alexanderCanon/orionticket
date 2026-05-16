# ── Build stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copiar el wrapper y el descriptor de dependencias primero para aprovechar
# el cache de capas de Docker: si pom.xml no cambia, las dependencias no
# se vuelven a descargar en builds sucesivos.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -q

# Copiar el código fuente y producir el JAR sin ejecutar los tests
COPY src src
RUN ./mvnw package -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Usuario sin privilegios para reducir la superficie de ataque
RUN addgroup -S orion && adduser -S orion -G orion

WORKDIR /app

# Sólo se copia el JAR final; el JDK y las fuentes quedan fuera de la imagen
COPY --from=builder /build/target/*.jar app.jar

USER orion

EXPOSE 8084

# Health check compatible con el perfil local (requiere Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-local}", \
    "-jar", "app.jar"]
