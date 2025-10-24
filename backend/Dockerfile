# Stage 1: Construir el .jar usando Maven con JDK 21
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# También necesitamos asegurarnos de que Maven use Java 21
# Lo hacemos explícitamente en el pom.xml (ver paso 2)
RUN mvn clean package -DskipTests

# Stage 2: Crear la imagen final para ejecutar usando JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
