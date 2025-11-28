# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy the POM file and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application, skipping tests
RUN mvn clean install -DskipTests

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/starter-kit-restapi-springboot-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 3000

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]