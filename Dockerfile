# Build stage
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN gradle dependencies --no-daemon || return 0

COPY src src

RUN gradle clean bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="UTEC Planificador Team"
LABEL description="Backend del Planificador Docente"
LABEL version="1.0.0"

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

ENV JAVA_OPTS_DEFAULT="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-$JAVA_OPTS_DEFAULT} -jar app.jar"]
