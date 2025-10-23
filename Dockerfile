FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/reviews-api-1.0.0.jar app.jar

EXPOSE 8084

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE="default"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
