# recipes-restAPI
> API REST que busca una receta por nombre mediante Spoonacular.

[![Java 21](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)

## ¿Qué hace esta API?
Expone un endpoint propio para buscar recetas por nombre y usa Spoonacular como proveedor.

## Instalación
### Requisitos previos
- JDK 21

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

## Endpoints
| Método | Endpoint | Descripción |
| --- | --- | --- |
| GET | `/api/recipes` | Buscar recetas por nombre |

### Parámetros de `GET /api/recipes`
| Parámetro | Obligatorio | Por defecto | Valores permitidos | Descripción |
| --- | --- | --- | --- | --- |
| `query` | Sí | No tiene | Texto no vacío | Nombre de la receta a buscar |
| `number` | No | `10` | 1-100 | Número máximo de resultados |
| `offset` | No | `0` | 0-900 | Número de resultados que se omiten, para paginar |

## Ejemplo de uso
Con la API en marcha, esta petición busca 2 recetas de pasta. También puedes abrir la URL directamente en el navegador.

### Entrada
```bash
curl "http://localhost:8080/api/recipes?query=pasta&number=2"
```

### Salida
Respuesta `200 OK`:

```json
{
  "results": [
    {
      "id": 657933,
      "title": "Ratatouille Pasta",
      "image": "https://img.spoonacular.com/recipes/657933-312x231.jpg"
    },
    {
      "id": 654959,
      "title": "Pasta With Tuna",
      "image": "https://img.spoonacular.com/recipes/654959-312x231.jpg"
    }
  ],
  "offset": 0,
  "number": 2,
  "totalResults": 231
}
```

## Errores
Los errores se devuelven en formato `application/problem+json`.

| Código | Cuándo ocurre | Qué hacer |
| --- | --- | --- |
| `400 Bad Request` | Falta `query`, está vacío o `number`/`offset` no son válidos | Corregir los parámetros de la petición |
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

## Extras
- [x] Implementar un mecanismo de caché para evitar llamadas repetidas a la API de Spoonacular
- [ ] Preparar el proyecto para ejecutarse en un contenedor Docker
- [ ] Incluir documentación Swagger para los endpoints
- [x] Añadir pruebas unitarias y/o de integración para los endpoints
- [x] Subir el proyecto a un repositorio público (por ejemplo, GitHub)
