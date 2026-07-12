# Build stage
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create a non-root user for security
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 appuser

# Copy JAR and set ownership
COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar

# Use non-root user
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]