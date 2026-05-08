# ── Build stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copiar archivos de Maven para cache de dependencias
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descargar dependencias (aprovecha el cache de Docker si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -q

# Copiar el código fuente y compilar el JAR sin tests
COPY src src
RUN ./mvnw package -DskipTests -q

# ── Runtime stage ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Usuario sin privilegios para mayor seguridad
RUN addgroup -S orion && adduser -S orion -G orion

WORKDIR /app

# Copiar sólo el JAR generado
COPY --from=builder /build/target/*.jar app.jar

# Cambiar al usuario sin privilegios
USER orion

# Puerto del servicio (coincide con server.port en application.yml)
EXPOSE 8082

# Health check para Docker Compose
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:default}", \
    "-jar", "app.jar"]
