# Build stage
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Copy everything
COPY . .

# Build the application - uses the repositories from pom.xml
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]