# Clematis Authorization API
[![CI to Docker Hub](https://github.com/grauds/clematis.auth.api/actions/workflows/CI_to_Docker_Hub.yml/badge.svg)](https://github.com/grauds/clematis.auth.api/actions/workflows/CI_to_Docker_Hub.yml)

Keycloak as a bean in Spring Boot environment

## About

A Spring Boot wrapper for Keycloak authorization server is based on [Thomas Darimont's project](https://github.com/thomasdarimont/embedded-spring-boot-keycloak-server). In addition to clustering features, this version is tailored for Clematis suite of applications and carries a default keycloak configuration for 'Clematis' realm. Other Clematis applications are tested to work with this configuration.

## Quick Start

Checkout the code

```
git clone https://github.com/grauds/clematis.auth.api.git
```
Set executable bit to gradlew
```
chmod +x gradlew
```
Run the build and test the configuration attached. Please note, that the configuration for 'Clematis' realm also includes some test data: users and applications.
```
./gradlew clean build
```
To pack the application into a Docker container run the Docker build
```
docker build -t clematis.auth.api .
```
The application is meant to be deployed with Docker compose along with MySQL database as a dependency. The suggested Docker compose configuration can be found [here](https://github.com/grauds/clematis.auth.api/blob/main/jenkins/docker-compose.yaml). There are many environment variables defined in the [.env](https://github.com/grauds/clematis.auth.api/blob/main/jenkins/.env) file, but if someone needs another environment to be configured, just replace the file with the actual one. 



