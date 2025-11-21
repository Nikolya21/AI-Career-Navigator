# Используйте multi-stage build с корректными именами
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Финальный образ - используйте openjdk для надежности
FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Создаем пользователя (для Linux)
RUN groupadd -r spring && useradd -r -g spring alcareer
USER alcareer

ENTRYPOINT ["java", "-jar", "/app/app.jar"]