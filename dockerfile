FROM maven:3.6-openjdk-11
COPY ./src /app/src
COPY ./pom.xml /app/pom.xml
WORKDIR /app
CMD mvn clean install

