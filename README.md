# recipes-restAPI
> API REST que busca una receta por nombre mediante Spoonacular.

[![Java 21](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)

## ¿Qué hace esta API?
Expone un endpoint propio para buscar recetas por nombre y usa Spoonacular como proveedor. Devuelve el primer resultado de la búsqueda.

## Tecnologías
- Java 21
- Spring Boot 4.1.2
- Caffeine (caché en memoria)
- springdoc-openapi (documentación Swagger)
- Gradle
- Docker

## Instalación
### Requisitos previos
- JDK 21 para ejecutarla en local, o Docker con Compose para ejecutarla en un contenedor

### 1. Clonar el repositorio
```bash
git clone https://github.com/ARPmaster/recipes-restAPI
cd recipes-restAPI
```

### 2. Configurar la API key
La API necesita una key de Spoonacular para buscar recetas.

1. Crea una cuenta en [Spoonacular](https://spoonacular.com/food-api/console) y copia tu API key desde el panel de tu perfil.
2. Crea un archivo `.env` en la raíz del proyecto con este contenido:

   ```dotenv
   SPOONACULAR_API_KEY=tu_api_key
   ```

> **Importante:** sin una key válida, la API arranca, pero las búsquedas devuelven un error `502 Bad Gateway`.

### 3. Ejecutar los tests
#### Linux
```bash
./gradlew test
```
#### Windows
```bash
gradlew.bat test
```

### 4. Arrancar la API
Ejecuta el comando de tu sistema. La API quedará disponible en `http://localhost:8080`.

#### Linux
```bash
./gradlew bootRun
```
#### Windows
```bash
gradlew.bat bootRun
```

### Alternativa: arrancar con Docker
Solo necesitas Docker con Compose y el archivo `.env` del paso 2; no hace falta tener instalado el JDK.

```bash
docker compose up --build
```

La API quedará disponible en `http://localhost:8080`. Para detenerla, ejecuta `docker compose down`.

La API key se pasa al contenedor desde `.env` al arrancar y no se guarda en la imagen.

## Endpoints
| Método | Endpoint | Descripción |
| --- | --- | --- |
| GET | `/api/recipes` | Buscar una receta por nombre |

### Parámetros de `GET /api/recipes`
| Parámetro | Obligatorio | Valores permitidos | Descripción |
| --- | --- | --- | --- |
| `query` | Sí | Texto no vacío | Nombre de la receta a buscar |

## Ejemplo de uso
Con la API en marcha, esta petición busca una receta de pasta. También puedes abrir la URL directamente en el navegador.

### Entrada
```bash
curl "http://localhost:8080/api/recipes?query=pasta"
```

### Salida
Respuesta `200 OK`. Los valores son ilustrativos y dependen de lo que devuelva Spoonacular:

```json
{
  "title": "Pasta con tomate",
  "readyInMinutes": 30,
  "servings": 4,
  "ingredients": [
    "400 g de pasta",
    "2 tomates"
  ],
  "instructions": [
    "Cuece la pasta.",
    "Mezcla la pasta con el tomate."
  ],
  "image": "https://example.com/pasta.jpg"
}
```

### Campos de la respuesta
| Campo | Tipo | Descripción |
| --- | --- | --- |
| `title` | Texto | Título de la receta |
| `readyInMinutes` | Número | Tiempo de preparación en minutos |
| `servings` | Número | Número de raciones |
| `ingredients` | Lista de textos | Ingredientes de la receta |
| `instructions` | Lista de textos | Pasos de la receta, en orden |
| `image` | Texto | URL de la imagen |

## Errores
Los errores se devuelven en formato `application/problem+json`.

| Código | Cuándo ocurre | Qué hacer |
| --- | --- | --- |
| `400 Bad Request` | Falta `query` o está vacío | Corregir los parámetros de la petición |
| `404 Not Found` | No hay ninguna receta para la búsqueda | Probar con otro nombre |
| `502 Bad Gateway` | Spoonacular rechaza la API key o no está disponible | Revisar la API key o volver a intentarlo más tarde |
| `503 Service Unavailable` | Se ha agotado la cuota de Spoonacular | Volver a intentarlo más tarde |

### Ejemplo
Petición sin el parámetro obligatorio `query`:

```bash
curl "http://localhost:8080/api/recipes"
```

Respuesta `400 Bad Request`:

```json
{
  "detail": "Required parameter 'query' is not present.",
  "instance": "/api/recipes",
  "status": 400,
  "title": "Bad Request"
}
```

## Documentación Swagger
Con la API en marcha, la documentación está disponible en:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI en JSON: `http://localhost:8080/v3/api-docs`

## Caché
Las búsquedas correctas se guardan en memoria para no repetir llamadas a Spoonacular:

- Cada búsqueda se conserva durante 10 minutos, con un máximo de 1000 entradas.
- No se distinguen mayúsculas ni espacios en los extremos: `Pasta` y ` pasta ` usan la misma entrada.
- Los errores y las búsquedas sin resultados no se guardan.

## Estructura del proyecto
```text
src/main/java/com/example/demo/
├── config/                         Configuración de la caché y de Spoonacular
├── controller/                     Endpoint REST
├── dto/                            Respuesta de la API
├── exception/                      Excepciones y conversión a respuestas de error
├── DemoApplication.java            Arranque de la aplicación
├── RecipeService.java              Lógica de negocio y caché
├── SpoonacularClient.java          Llamadas a Spoonacular
└── SpoonacularSearchResponse.java  Respuesta recibida de Spoonacular
```

## Extras
- [x] Implementar un mecanismo de caché para evitar llamadas repetidas a la API de Spoonacular
- [x] Preparar el proyecto para ejecutarse en un contenedor Docker
- [x] Incluir documentación Swagger para los endpoints
- [x] Añadir pruebas unitarias y/o de integración para los endpoints
- [x] Subir el proyecto a un repositorio público (por ejemplo, GitHub)
