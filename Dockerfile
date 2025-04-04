# Imagen base oficial con JDK 21
FROM eclipse-temurin:21-jdk

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar todo el contenido del proyecto
COPY . .

# Dar permisos al mvnw
RUN chmod +x mvnw

# Compilar el proyecto (sin tests para que sea rápido)
RUN ./mvnw clean package -DskipTests

# Comando para arrancar la app
CMD ["java", "-jar", "target/CeleraAi-0.0.1-SNAPSHOT.jar"]
