# --- build stage ---
FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /workspace

COPY pom.xml .
COPY department-service/pom.xml department-service/pom.xml
COPY person/pom.xml person/pom.xml
RUN mvn -q -DskipTests dependency:go-offline

COPY department-service/src department-service/src
COPY person/src person/src

RUN mvn -q -DskipTests -pl department-service -am clean package
# BOOT jar: finalName = department-service -> department-service.jar
RUN cp department-service/target/department-service-*.jar /tmp/app.jar

# --- runtime ---
FROM eclipse-temurin:23-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /tmp/app.jar /app/app.jar
ENTRYPOINT ["sh","-c","java ${JAVA_OPTS} -jar /app/app.jar"]
