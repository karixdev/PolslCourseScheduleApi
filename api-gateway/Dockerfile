FROM maven:3.8.7-eclipse-temurin-17 AS builder

COPY pom.xml /app/
COPY src /app/src
RUN mvn -f /app/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]