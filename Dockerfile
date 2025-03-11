# Use a Maven base image to build the project
FROM maven:3.9.9-eclipse-temurin-23 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code into the container
COPY pom.xml .

# Download the Maven dependencies (this helps cache dependencies if unchanged)
RUN mvn dependency:go-offline

# Copy the entire source code
COPY src ./src

# Build the project and package the jar
RUN mvn clean package -DskipTests

# Use a Java runtime image for the final app
FROM eclipse-temurin:23-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file from the build container into the final container
COPY --from=build /app/target/todoist-api-1.0.0.jar todoist-api-1.0.0.jar

# Expose the port your app will run on (default Spring Boot port)
EXPOSE 8080

# Command to run the app
ENTRYPOINT ["java", "-jar", "todoist-api-1.0.0.jar"]
