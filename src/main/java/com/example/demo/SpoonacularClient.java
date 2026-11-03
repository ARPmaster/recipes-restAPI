package com.example.demo;

import java.net.URI;
import java.util.Map;

import com.example.demo.exception.SpoonacularException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import com.example.demo.exception.SpoonacularException.Reason;

/** Único punto de la aplicación que conoce la API de Spoonacular. */
@Component
public class SpoonacularClient {

	private final RestClient restClient;

	/**
	 * Constructor que usa Spring al arrancar la aplicación.
	 *
	 * @param properties URL base y clave de la API de Spoonacular
	 */
	@Autowired
	public SpoonacularClient(SpoonacularProperties properties) {
		this(properties, RestClient.builder());
	}

	/**
	 * Prepara el cliente HTTP. Recibir el builder permite a los tests usar un servidor simulado.
	 *
	 * @param properties URL base y clave de la API de Spoonacular
	 * @param builder constructor del cliente HTTP
	 */
	SpoonacularClient(SpoonacularProperties properties, RestClient.Builder builder) {
		this.restClient = builder
				.baseUrl(properties.baseUrl())
				.defaultHeader("x-api-key", properties.apiKey())
				.build();
	}

	/**
	 * Llama a Spoonacular para buscar recetas.
	 *
	 * @param query texto a buscar
	 * @return la respuesta de Spoonacular; su lista de resultados puede estar vacía
	 * @throws SpoonacularException si Spoonacular responde con error, no se puede conectar o la respuesta no es válida
	 */
	SpoonacularSearchResponse searchRecipes(String query) {
		SpoonacularSearchResponse response;
		try {
			response = restClient.get()
					.uri(uri -> buildSearchUri(uri, query))
					.retrieve()
					.onStatus(HttpStatusCode::isError, (request, upstream) -> {
						throw toException(upstream.getStatusCode());
					})
					.body(SpoonacularSearchResponse.class);
		}
		catch (RestClientException ex) {
			// Sin conexión, timeout o cuerpo que no se puede leer.
			throw new SpoonacularException(Reason.UPSTREAM_ERROR, "No se pudo completar la llamada a Spoonacular", ex);
		}
		if (response == null) {
			throw new SpoonacularException(Reason.UPSTREAM_ERROR, "Spoonacular devolvió una respuesta vacía");
		}
		return response;
	}

	/**
	 * Construye la URL de la búsqueda: pide un solo resultado con su información, ingredientes e instrucciones.
	 *
	 * @param uri constructor de URLs que ya incluye la dirección base
	 * @param query texto a buscar; se codifica para que los caracteres especiales no rompan la URL
	 * @return la URL completa de la búsqueda
	 */
	private URI buildSearchUri(UriBuilder uri, String query) {
		return uri.path("/recipes/complexSearch")
				.queryParam("query", "{query}")
				.queryParam("number", 1)
				.queryParam("addRecipeInformation",true)
				.queryParam("fillIngredients",true)
				.queryParam("addRecipeInstructions",true)
				.build(Map.of("query", query ));
	}

	/**
	 * Convierte un código de error HTTP de Spoonacular en nuestra excepción.
	 *
	 * @param status código HTTP recibido: 401 es clave inválida, 402 y 429 cuota agotada, y el resto error del proveedor
	 * @return la excepción con el motivo correspondiente
	 */
	private static SpoonacularException toException(HttpStatusCode status) {
		String message = "Spoonacular respondió " + status.value();
		return switch (status.value()) {
			case 401 -> new SpoonacularException(Reason.INVALID_API_KEY, message);
			case 402, 429 -> new SpoonacularException(Reason.QUOTA_EXCEEDED, message);
			default -> new SpoonacularException(Reason.UPSTREAM_ERROR, message);
		};
	}
}
