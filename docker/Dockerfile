FROM openjdk:8-jdk-alpine

LABEL maintainer="ivangfr@yahoo.com.br"

RUN apk update && apk add ca-certificates wget && update-ca-certificates

ADD springboot-mesos-marathon-keycloak-openldap*.jar /simple-service.jar

CMD ["java", "-jar", "/simple-service.jar"]

EXPOSE 8080