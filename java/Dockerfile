FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY proxy-api/pom.xml .
COPY proxy-api/mvnw .
COPY proxy-api/.mvn ./.mvn

RUN apk add --no-cache curl

COPY proxy-api/src ./src

RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /build/target/proxy-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --retries=3 --start-period=15s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
