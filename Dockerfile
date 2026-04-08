# Use Maven to build the project
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Use a minimal JRE image for the runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /root/

# Copy the jar file from the builder
COPY --from=builder /app/target/catalog-api-0.0.1-SNAPSHOT.jar ./api.jar

# Expose port (default 8080 can be overridden via PORT env var)
EXPOSE 8080

# Command to run the executable
CMD ["java", "-jar", "./api.jar"]
