FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy gradle wrapper and config files first for better caching and reliability
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY settings.gradle build.gradle ./

# Download dependencies (cacheable layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src/ src/

# Build the application using gradle wrapper
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
