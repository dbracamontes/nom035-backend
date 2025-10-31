# Usa una imagen oficial de Java 21 como base
# Usa una imagen oficial de Java 21 como base
FROM eclipse-temurin:21-jdk-alpine

# Crear y usar el directorio de trabajo
WORKDIR /app

# Copia el jar generado por Maven/Gradle al contenedor
COPY target/nom035-backend-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto estándar de Spring Boot
EXPOSE 8080

# Variables de entorno. Puedes sobreescribirlas en docker-compose.yml
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/nom035?useSSL=false&serverTimezone=UTC
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=your_mysql_password

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]