# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Copia el archivo de variables de entorno (opcional, para debug)
COPY .env.dev .env.dev

# Puerto que expone la app
EXPOSE 80

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
