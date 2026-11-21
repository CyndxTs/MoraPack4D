# Imagen para Render (Render no tiene runtime nativo de Java: se despliega con Docker)
# --- Etapa 1: compilar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# --- Etapa 2: ejecutar ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Carpeta de los reportes .txt (en Render es temporal: se borra al reiniciar)
RUN mkdir -p exports
ENV SPRING_PROFILES_ACTIVE=tidb
# Usa hasta el 75% de la RAM del contenedor (el plan gratuito de Render tiene poca)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Duser.timezone=UTC"
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
