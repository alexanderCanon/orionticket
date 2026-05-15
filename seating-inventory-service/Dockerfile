# Build en dos etapas:
# 1. Compilar y empaquetar el JAR con Maven
# 2. Imagen final mínima solo con el JAR (no incluye Maven ni fuentes)

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Descarga dependencias primero (aprovecha caché de Docker si pom.xml no cambió)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
