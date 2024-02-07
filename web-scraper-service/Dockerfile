ARG JAVA_VERSION=17
FROM openjdk:${JAVA_VERSION}
COPY target/*.jar app.jar
EXPOSE 8080
CMD ["java","-jar","/app.jar"]