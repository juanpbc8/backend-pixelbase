# ==========================================
# ETAPA 1: Compilación (Build Stage)
# ==========================================
# Imagen de Maven con JDK 21 corriendo sobre Ubuntu (Jammy)
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Copiar el POM y descargar dependencias en caché para acelerar futuros despliegues
COPY pom.xml .
# Si tu pom.xml no cambia, los despliegues futuros tardarán segundos en lugar de minutos.
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar el archivo JAR saltando los tests
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# ETAPA 2: Entorno de Ejecución Ligero (Run Stage)
# ==========================================
# Ya no necesitamos Maven ni el compilador, solo el JRE para ejecutar el JAR.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar únicamente el JAR compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Ajuste dinámico y seguro para entornos desde 512MB (Render) hasta Droplets más grandes
ENV JAVA_OPTS="-XX:MaxRAMPercentage=60.0 -XX:MinRAMPercentage=50.0 -Xss512k -Dfile.encoding=UTF-8"

# Exponemos el puerto estándar donde escucha Spring Boot
EXPOSE 8080

# Ejecutar la aplicación inyectando las flags de memoria
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
