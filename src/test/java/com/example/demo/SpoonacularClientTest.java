package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.List;

import com.example.demo.config.SpoonacularProperties;
import com.example.demo.exception.SpoonacularException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.example.demo.exception.SpoonacularException.Reason;

class SpoonacularClientTest {

	private static final String RECIPE_PARAMS =
			"&number=1&addRecipeInformation=true&fillIngredients=true&addRecipeInstructions=true";

	private static final String SEARCH_URL =
			"https://api.test/recipes/complexSearch?query=pasta" + RECIPE_PARAMS;

	private MockRestServiceServer server;

	private SpoonacularClient client;

	/** Antes de cada test crea el cliente conectado a un servidor simulado. */
	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		client = new SpoonacularClient(new SpoonacularProperties("test-key", "https://api.test"), builder);
	}

	/** Comprueba que la clave va en la cabecera y que la respuesta se interpreta correctamente. */
	@Test
	void sendsKeyInHeaderAndParsesResponse() {
		// Mismos nombres y anidación que una respuesta real de complexSearch, incluidos campos que ignoramos.
		server.expect(requestTo(SEARCH_URL))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header("x-api-key", "test-key"))
				.andRespond(withSuccess("""
						{
						  "results": [
						    {
						      "id": 657933,
						      "title": "Ratatouille Pasta",
						      "image": "https://img/657933.jpg",
						      "imageType": "jpg",
						      "readyInMinutes": 45,
						      "preparationMinutes": null,
						      "cookingMinutes": null,
						      "servings": 4,
						      "extendedIngredients": [
						        {"id": 11282, "name": "onion", "original": "1 large Yellow Onion, diced", "amount": 1.0, "unit": "large"}
						      ],
						      "analyzedInstructions": [
						        {"name": "", "steps": [{"number": 1, "step": "Saute the onion.", "ingredients": [], "equipment": []}]}
						      ]
						    }
						  ],
						  "offset": 0,
						  "number": 1,
						  "totalResults": 231
						}
						""", MediaType.APPLICATION_JSON));

		SpoonacularSearchResponse response = client.searchRecipes("pasta");

		server.verify();
		assertThat(response.results()).containsExactly(new SpoonacularSearchResponse.Result(
				657933L, "Ratatouille Pasta", "https://img/657933.jpg", 45, 4,
				List.of(new SpoonacularSearchResponse.Ingredient("1 large Yellow Onion, diced")),
				List.of(new SpoonacularSearchResponse.InstructionBlock(
						List.of(new SpoonacularSearchResponse.Step("Saute the onion."))))));
	}

	/** Comprueba que los caracteres especiales de la búsqueda se codifican en la URL. */
	@Test
	void encodesSpecialCharactersInQuery() {
		server.expect(requestTo(
				"https://api.test/recipes/complexSearch?query=mac%20%26%20cheese%2Bextra" + RECIPE_PARAMS))
				.andRespond(withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));

		client.searchRecipes("mac & cheese+extra");

		server.verify();
	}

	/**
	 * Comprueba que cada código de error HTTP se convierte en el motivo adecuado.
	 *
	 * @param status código HTTP que responde el servidor simulado
	 * @param expected motivo que se espera en la excepción
	 */
	@ParameterizedTest
	@CsvSource({
			"401, INVALID_API_KEY",
			"402, QUOTA_EXCEEDED",
			"429, QUOTA_EXCEEDED",
			"400, UPSTREAM_ERROR",
			"500, UPSTREAM_ERROR"
	})
	void translatesErrorStatus(int status, Reason expected) {
		server.expect(requestTo(SEARCH_URL)).andRespond(withStatus(HttpStatus.valueOf(status)));

		assertFailsWith(expected);
	}

	/** Comprueba que un fallo de conexión se convierte en UPSTREAM_ERROR. */
	@Test
	void translatesConnectionFailure() {
		server.expect(requestTo(SEARCH_URL)).andRespond(withException(new IOException("sin conexión")));

		assertFailsWith(Reason.UPSTREAM_ERROR);
	}

	/** Comprueba que una respuesta ilegible se convierte en UPSTREAM_ERROR. */
	@Test
	void translatesUnreadableBody() {
		server.expect(requestTo(SEARCH_URL)).andRespond(withSuccess("no es json", MediaType.APPLICATION_JSON));

		assertFailsWith(Reason.UPSTREAM_ERROR);
	}

	/**
	 * Lanza una búsqueda y comprueba que falla con el motivo indicado.
	 *
	 * @param expected motivo que se espera en la excepción
	 */
	private void assertFailsWith(Reason expected) {
		assertThatThrownBy(() -> client.searchRecipes("pasta"))
				.isInstanceOfSatisfying(SpoonacularException.class,
						ex -> assertThat(ex.getReason()).isEqualTo(expected));
	}
}
