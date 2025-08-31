# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copia solo los archivos necesarios para las dependencias primero (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el código fuente
COPY src ./src

# Construye la aplicación
RUN mvn clean package -DskipTests -B

# Etapa de ejecución
FROM eclipse-temurin:21.0.2_13-jre-jammy AS runtime

# Instala dumb-init para manejo correcto de señales
RUN apt-get update && apt-get install -y --no-install-recommends dumb-init wget && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Crea usuario no-root para seguridad
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/bash -m appuser

WORKDIR /app

# Copia el JAR desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Cambia la propiedad del archivo al usuario no-root
RUN chown appuser:appgroup app.jar

# Cambia al usuario no-root
USER appuser

# Configura el puerto
EXPOSE 8080

# Variables de entorno para optimización de JVM en contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+DisableExplicitGC -Djava.security.egd=file:/dev/./urandom"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider --timeout=5 http://localhost:8080/actuator/health || exit 1

# Usa dumb-init como PID 1 para manejo correcto de señales
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
