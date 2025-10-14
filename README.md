# spring-microservice
`mvn spring-boot:run` - start a Spring Boot application using Maven.

# Build project
mvn clean package -DskipTests

# Build docker
* `docker build . --tag licensing-service`  || `mvn package dockerfile:build`

# Run Project
- Build project
- Build Docker image 
- Find docker images exist --> `docker images`
```text
REPOSITORY                      TAG                     IMAGE ID            CREATED                 SIZE
ostock/licensing-service        0.0.1-SNAPSHOT          906f6baeaede        About a minute ago      483MB
```
- Run Docker image --> `docker run -it -p8080:8080 licensing-service:latest` || `docker run ostock/licensing-service:0.0.1-SNAPSHOT`

------------------------------------------------------------------------------------------------------------------------

# How to connect both Licensing-service & Config-Service
## Local host setup
1. Start up the Config-service
   `mvn spring-boot:run`
2. After confirmed that config-server is up & running
   ```text
   Can make a postman call to this url: `http://localhost:8071/licensing-service/dev`
    ```
3. Start up the Licensing-service
   ` mvn spring-boot:run`
   - Build if needed.
    ` mvn spring-boot:run`

## With docker containers
tbd