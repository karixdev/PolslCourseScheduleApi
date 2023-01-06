FROM openjdk:17

WORKDIR /var/www/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw package -Dmaven.test.skip

COPY target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]