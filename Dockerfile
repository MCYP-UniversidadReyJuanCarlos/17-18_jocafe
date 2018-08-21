FROM maven as builder
COPY . /code/
WORKDIR /code
RUN mvn package -Dmaven.test.skip=true

FROM openjdk:8-jre
COPY --from=builder /code/target/*.jar /usr/app/
WORKDIR /usr/app
CMD [ "java", "-jar", "security-0.1.0.jar" ]
