# Etapa de compilación: genera el jar con el wrapper de Gradle del proyecto.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Se copian primero los ficheros de Gradle para reutilizar las dependencias
# descargadas mientras no cambie build.gradle.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon --quiet

COPY src src
RUN ./gradlew bootJar --no-daemon

# Etapa de ejecución: solo contiene el JRE y el jar, y no se ejecuta como root.
FROM eclipse-temurin:21-jre
RUN useradd --system --no-create-home spring
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
