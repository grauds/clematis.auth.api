FROM openjdk:17-jdk-slim

ARG JAR_FILE=build/**/clematis.auth.api-*.jar
COPY ${JAR_FILE} app.jar

RUN mkdir -p /var/log/clematis

ENTRYPOINT ["java","-jar","/app.jar"]
