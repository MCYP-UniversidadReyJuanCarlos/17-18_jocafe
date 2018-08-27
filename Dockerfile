# FROM maven as builder
# COPY . /code/
# WORKDIR /code
# RUN mvn package -Dmaven.test.skip=true

FROM owasp/zap2docker-stable:latest AS zap

FROM ahannigan/docker-arachni AS arachni

FROM andresriancho/w3af:develop AS w3af

# Update before installing any package
RUN apt-get update -y

# Install JDK 1.8 
RUN apt-get install -y python-software-properties
RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get update -y
RUN apt-get install -y openjdk-8-jdk

COPY --from=zap /zap /zap
COPY --from=arachni /arachni /arachni
#COPY --from=w3af . /w3af
COPY target/*.jar /usr/app/
#WORKDIR /usr/app
#CMD [ "java", "-jar", "security-0.1.0.jar" ]
COPY start.sh start.sh
CMD [ "sh", "start.sh" ]