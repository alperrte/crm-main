# --- build stage ---
FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /workspace

# Multi-module için kök ve tüm modül POM'larını kopyala (cache verimli olsun)
COPY pom.xml .
COPY department-service/pom.xml department-service/pom.xml
COPY person/pom.xml person/pom.xml
COPY user-service/pom.xml user-service/pom.xml

# Bağımlılıkları cache'e al
RUN mvn -q -DskipTests dependency:go-offline

# Kaynak kodlar (hepsini kopyalıyoruz ki -am bağımlıları da derleyebilsin)
COPY department-service/src department-service/src
COPY person/src person/src
COPY user-service/src user-service/src

# Sadece department-service'i (bağımlıları ile) derle
RUN mvn -q -DskipTests -pl department-service -am clean package

# Üretilen JAR'ı sabit bir yere kopyala
RUN cp department-service/target/department-service-*.jar /tmp/app.jar

# --- runtime stage ---
FROM eclipse-temurin:23-jre
WORKDIR /app

# Healthcheck için curl
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /tmp/app.jar /app/app.jar

# ÖNEMLİ: Exec form ENTRYPOINT (sh -c değil) => container foreground'da çalışır
ENTRYPOINT ["java","-jar","/app/app.jar"]
