# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY gradle* /app/
COPY . .

RUN chmod +x gradlew && \
    ./gradlew clean build -x test

# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
