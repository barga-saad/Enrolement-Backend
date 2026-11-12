# Build stage
FROM repo-prod.gbp.cpm.net:6000/openjdk:17-jdk-slim-bullseye AS builder

WORKDIR /app

COPY . .

#RUN chmod +x gradlew && ./gradlew clean build -x test --no-daemon

# Production stage
FROM repo-prod.gbp.cpm.net:6000/openjdk:17-jdk-slim-bullseye

WORKDIR /app

COPY --from=builder /app/build/libs/dl-ms-enrolment-*.jar /app/app.jar
COPY 10.2.60.146.crt /tmp/10.2.60.146.crt

RUN keytool -importcert -trustcacerts -file /tmp/10.2.60.146.crt \
    -alias bcp-sso -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit -noprompt

EXPOSE 8094

ENTRYPOINT ["java", "-jar", "app.jar"]
