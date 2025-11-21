FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/target/*.jar app.jar

# Создаем пользователя для безопасности
RUN addgroup -S spring && adduser -S aicareer -G spring
USER aicareer

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]