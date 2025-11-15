# Orvix Gateway

**Orvix Gateway** is a Spring Boot based microservice using Spring Cloud and WebFlux which relies on **Project Reactor**.

⚠️⚠️⚠️ Caution: This is just the beginning. The gateway is not yet functional; development is ongoing.

## Tech stack

    - Java 21
    - maven 3.9.5
    - Spring Boot 3.3.4

## Build and Run

   1. **Build the project, downloading the dependencies, and run the tests**

    mvn clean install

    mvn test

   2. **Package the project into a fat/executable JAR**

    mvn clean package

   3. **Run the application via Maven**

    mvn spring-boot:run    

   4. **Run directly the packaged JAR**

    java -jar target/orvix-gateway-0.0.1-SNAPSHOT.jar

⚠️⚠️⚠️ Make sure you are in the root directory of the project when executing these commands.