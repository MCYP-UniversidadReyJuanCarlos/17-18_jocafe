FROM openjdk:8-jre
COPY target/*.jar /usr/app/
WORKDIR /usr/app
CMD [ "java", "-jar", "security-0.1.0.jar" ]
