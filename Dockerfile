#stage 1
#Start with a base image containing Java runtime
# FROM eclipse-temurin:17-jdk-alpine as build

# Add Maintainer Info
# LABEL maintainer="Illary Huaylupo <illaryhs@gmail.com>"

# The application's jar file
# ARG JAR_FILE

# Add the application's jar to the container
# COPY ${JAR_FILE} app.jar

#unpackage jar file
# RUN mkdir -p target/dependency && (cd target/dependency; jar -xf /app.jar)

#stage 2
#Same Java runtime
# FROM eclipse-temurin:17-jdk-alpine

#Add volume pointing to /tmp
# VOLUME /tmp

#Copy unpackage application to new container
# ARG DEPENDENCY=/target/dependency
# COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
# COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
# COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

#excute the application
# ENTRYPOINT ["java", "-cp","app:app/lib/*","com.optimagrowth.license.LicensingServiceApplication"]
# ENTRYPOINT ["java", "-cp", ".:lib/*", "com.optimagrowth.license.LicenseServiceApplication"]

# ~~~~~~~~~~~~~~~~~~~~~ Layered JARs ~~~~~~~~~~~~~~~~~~~~~
FROM openjdk:17-slim as build
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:11-slim
WORKDIR application
COPY --from=build application/dependencies/ ./
COPY --from=build application/spring-boot-loader/ ./
COPY --from=build application/snapshot-dependencies/ ./
COPY --from=build application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]