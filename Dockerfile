# Берем готовый образ с Tomcat + Java
FROM tomcat:10.1-jdk17

# Очищаем дефолтные приложения (чтобы было только наше)
RUN rm -rf /usr/local/tomcat/webapps/*

# Копируем наш WAR в папку развертывания Tomcat
# ROOT.war = приложение будет доступно по корневому URL (http://localhost:8080/)
COPY ./target/*.war /usr/local/tomcat/webapps/ROOT.war

# Открываем порт
EXPOSE 8080

# Команда запуска уже встроена в образ Tomcat!