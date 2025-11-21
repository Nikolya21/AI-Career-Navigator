FROM amazoncorretto:17-alpine
WORKDIR /app
COPY target/*.war app.war
ENTRYPOINT ["java", "-war", "app.war"]