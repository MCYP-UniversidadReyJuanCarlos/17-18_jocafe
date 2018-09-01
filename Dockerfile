FROM owasp/zap2docker-stable:latest AS zap

FROM ahannigan/docker-arachni AS arachni

# W3af will be the base image
FROM andresriancho/w3af:develop

# Update before installing any package
RUN apt-get update -y

# Install JDK 1.8 
RUN apt-get install -y python-software-properties
RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get update -y
RUN apt-get install -y openjdk-8-jdk

# Install Maven
RUN apt-get install -y maven

COPY --from=zap /zap /zap
COPY --from=arachni /arachni /arachni

COPY . /code/
WORKDIR /code
RUN mvn package -Dmaven.test.skip=true

WORKDIR /
COPY start.sh start.sh
CMD [ "sh", "start.sh" ]