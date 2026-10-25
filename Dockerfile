FROM eclipse-temurin:21-jre
#base image Java 21 -> must be same as project, also 21-jre is only needed for runtime
#If you dont use -jre you get the full devkit, the image does not need this

WORKDIR /app
# Builds directory /app in image en every following command will be build there

RUN useradd appuser
# makes sure that root doesnt run it but a user, security-wise

COPY target/mini-payments-api-0.0.1-SNAPSHOT.jar app.jar
# Copies build jar file from host to images as app.jar

USER appuser
# Which user runs the commands.

EXPOSE 8080
# exposes correct port

ENTRYPOINT [ "java" , "-jar", "app.jar" ]
# Command that runs