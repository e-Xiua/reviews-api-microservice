# Stage 1: Dependencies
FROM maven:3.9.6-eclipse-temurin-17-alpine AS dependencies
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Stage 2: Test (optional)
FROM dependencies AS test
COPY src ./src
RUN mvn clean test --no-transfer-progress

# Stage 3: Build
FROM dependencies AS build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 4: Runtime (FIXED - all commands INSIDE this stage)
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Install tools (inside the stage)
RUN apk add --no-cache curl wget postgresql-client

# Copy JAR from build stage (inside the stage)
COPY --from=build /app/target/reviews-api-*.jar /app/reviews.jar

# Create user (inside the stage)
RUN addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app
USER spring:spring

# Expose port
EXPOSE 8084

# Default profile
ENV SPRING_PROFILES_ACTIVE=postgres
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Run
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/reviews.jar"]
