# Multi-stage Dockerfile for SmartCart AI Agent Backend
# Optimized for free-tier hosting platforms (Render, Railway, Fly.io)

# Stage 1: Build JAR using Maven and JDK 17
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and configuration
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies offline for faster caching
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production fat JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal JRE 17 runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy compiled JAR from build stage
COPY --from=build /app/target/smartcart-ai-agent-0.0.1-SNAPSHOT.jar app.jar

# JVM memory flags tuned for 512 MB free-tier limits
ENV JAVA_OPTS="-XX:+UseSerialGC -Xmx384m -Dfile.encoding=UTF-8"
ENV PORT=8081
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
