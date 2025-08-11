# --- Build stage ---
FROM maven:3.9-eclipse-temurin-23 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests clean package

# --- Run stage ---
FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /app/target/department-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]