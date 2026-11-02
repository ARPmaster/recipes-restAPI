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

	@Autowired
	public SpoonacularClient(SpoonacularProperties properties) {
		this(properties, RestClient.builder());
	}

	SpoonacularClient(SpoonacularProperties properties, RestClient.Builder builder) {
		this.restClient = builder
				.baseUrl(properties.baseUrl())
				.defaultHeader("x-api-key", properties.apiKey())
				.build();
	}

	SpoonacularSearchResponse searchRecipes(String query, int number, int offset) {
		SpoonacularSearchResponse response;
		try {
			response = restClient.get()
					.uri(uri -> buildSearchUri(uri, query, number, offset))
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

	private URI buildSearchUri(UriBuilder uri, String query, int number, int offset) {
		return uri.path("/recipes/complexSearch")
				.queryParam("query", "{query}")
				.queryParam("number", "{number}")
				.queryParam("offset", "{offset}")
				.build(Map.of("query", query, "number", number, "offset", offset));
	}

	private static SpoonacularException toException(HttpStatusCode status) {
		String message = "Spoonacular respondió " + status.value();
		return switch (status.value()) {
			case 401 -> new SpoonacularException(Reason.INVALID_API_KEY, message);
			case 402, 429 -> new SpoonacularException(Reason.QUOTA_EXCEEDED, message);
			default -> new SpoonacularException(Reason.UPSTREAM_ERROR, message);
		};
	}
}
