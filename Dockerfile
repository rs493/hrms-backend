# Build stage — Java 21 (matches pom.xml <java.version>)
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B clean package -DskipTests

# Runtime stage — lightweight JRE only
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/backend-*.jar app.jar

EXPOSE 8080

# Port comes from Render PORT via application.yml: server.port=${PORT:8080}
ENTRYPOINT ["java", "-jar", "app.jar"]
