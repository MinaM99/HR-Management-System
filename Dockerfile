# Multi-stage Dockerfile for HR Management System with Maven Build

# Build Stage - Maven build with Eclipse Temurin JDK 17
FROM eclipse-temurin:17-jdk-alpine AS build

# Install Maven manually to avoid X11 dependencies
RUN apk add --no-cache curl tar bash \
    && mkdir -p /usr/share/maven \
    && curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.4/binaries/apache-maven-3.9.4-bin.tar.gz \
        | tar -xzC /usr/share/maven --strip-components=1 \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# Set metadata
LABEL maintainer="HR Management System Team"
LABEL description="HR Management System Spring Boot Application"
LABEL version="1.0.0"

# Set the working directory for build
WORKDIR /build

# Copy pom.xml first for better Docker layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (clean package)
RUN mvn clean package -DskipTests

# Runtime Stage - Lightweight JRE image
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Create a non-root user for security
RUN addgroup -g 1000 hrms && adduser -u 1000 -G hrms -s /bin/sh -D hrms

# Copy the built JAR from build stage (note the /build path)
COPY --from=build /build/target/hr-management-system-1.0.0.jar app.jar

# Change ownership of the app directory to hrms user
RUN chown -R hrms:hrms /app

# Switch to non-root user
USER hrms

# Expose the port the app runs on
EXPOSE 8080

# Environment variables for production
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]