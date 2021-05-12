FROM openjdk:11-jdk
ARG JAR_FILE=build/**/clematis.auth.api-*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
