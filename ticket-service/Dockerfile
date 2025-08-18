# --- build stage ---
FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /workspace

COPY pom.xml .
COPY department-service/pom.xml department-service/pom.xml
COPY person/pom.xml person/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY ticket-service/pom.xml ticket-service/pom.xml

RUN mvn -q -DskipTests dependency:go-offline

COPY department-service/src department-service/src
COPY person/src person/src
COPY user-service/src user-service/src
COPY ticket-service/src ticket-service/src

RUN mvn -q -DskipTests -pl ticket-service -am clean package
RUN cp ticket-service/target/*-SNAPSHOT.jar /tmp/app.jar

# --- runtime stage ---
FROM eclipse-temurin:23-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /tmp/app.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
