# stage 1: Building image containing both Maven and Java 21
FROM maven:3.9.9-eclipse-temurin-21 as BUILD

WORKDIR /app

COPY . .

RUN mvn package -DskipTests

# stage 2: runtime 
FROM eclipse-temurin:21-jre-alpine as RUNTIME

WORKDIR /app

RUN adduser -S appuser

COPY --from=BUILD /app/target/*-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]