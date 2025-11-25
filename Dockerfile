# Build stage
FROM gradle:8.5-jdk21-jammy AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Download dependencies (cached layer if build.gradle doesn't change)
RUN gradle dependencies --no-daemon --refresh-dependencies

COPY src src

RUN gradle clean bootJar --no-daemon -x test

# Runtime stage
FROM ubuntu:24.04 AS runtime

# Instalar dependencias necesarias
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        openjdk-21-jre-headless \
        tzdata \
        curl \
        ca-certificates && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV TZ=America/Montevideo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

LABEL maintainer="UTEC Planificador Team"
LABEL description="Backend del Planificador Docente - Spring Boot REST API"
LABEL version="1.0.0"
LABEL org.opencontainers.image.title="UTEC Planificador Backend"
LABEL org.opencontainers.image.description="Sistema de planificación docente para UTEC"
LABEL org.opencontainers.image.vendor="Universidad Tecnológica del Uruguay"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.source="https://github.com/salvadorvanoli/utec-planificador-be"

RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

ENV JAVA_OPTS_DEFAULT="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-$JAVA_OPTS_DEFAULT} -jar app.jar"]
