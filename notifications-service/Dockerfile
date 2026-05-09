FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home spring
USER spring
COPY --from=build /workspace/target/notifications-service-*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
