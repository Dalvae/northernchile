# Multi-stage Dockerfile para producción
# Stage 1: Build con Maven y JDK 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

# Copiar solo pom.xml primero para aprovechar la caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime con JRE mínimo
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Actualizar tzdata para asegurar horarios de Chile correctos
# Chile cambia DST frecuentemente, necesitamos tzdata actualizado
RUN apk add --no-cache tzdata

# Copiar el JAR compilado desde la etapa de build
COPY --from=build /build/target/*.jar app.jar

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Exponer puerto
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
