FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY --from=build /app/target/todoist-api-1.0.0.jar todoist-api-1.0.0.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "todoist-api-1.0.0.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
