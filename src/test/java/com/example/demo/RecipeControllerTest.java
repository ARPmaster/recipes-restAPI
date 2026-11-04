package com.example.demo;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.List;

import com.example.demo.controller.RecipeController;
import com.example.demo.exception.RecipeNotFoundException;
import com.example.demo.exception.SpoonacularException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.exception.SpoonacularException.Reason;
import com.example.demo.dto.RecipeSearchResponse;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RecipeService recipeService;

	/** Comprueba que una búsqueda correcta devuelve un 200 con todos los campos de la receta. */
	@Test
	void returnsRecipe() throws Exception {
		when(recipeService.search("pasta")).thenReturn(new RecipeSearchResponse(
				"Ratatouille Pasta", 45, 4,
				List.of("1 large Yellow Onion, diced", "2 cups pasta"),
				List.of("Saute the onion.", "Boil the pasta."),
				"https://img/1.jpg"));

		mockMvc.perform(get("/api/recipes").param("query", "pasta"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Ratatouille Pasta"))
				.andExpect(jsonPath("$.readyInMinutes").value(45))
				.andExpect(jsonPath("$.servings").value(4))
				.andExpect(jsonPath("$.ingredients.length()").value(2))
				.andExpect(jsonPath("$.ingredients[0]").value("1 large Yellow Onion, diced"))
				.andExpect(jsonPath("$.instructions.length()").value(2))
				.andExpect(jsonPath("$.instructions[1]").value("Boil the pasta."))
				.andExpect(jsonPath("$.image").value("https://img/1.jpg"));
	}

	/** Comprueba que una receta sin pasos devuelve una lista de instrucciones vacía. */
	@Test
	void returnsEmptyInstructionsWhenRecipeHasNone() throws Exception {
		when(recipeService.search("pasta")).thenReturn(new RecipeSearchResponse(
				"Pasta", 45, 4, List.of("pasta"), List.of(), "https://img/1.jpg"));

		mockMvc.perform(get("/api/recipes").param("query", "pasta"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.instructions").isArray())
				.andExpect(jsonPath("$.instructions").isEmpty());
	}

	/**
	 * Comprueba que una búsqueda ausente o en blanco devuelve un 400 sin llamar al servicio.
	 *
	 * @param queryString parámetros de la petición que se quieren probar
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"",
			"query=",
			"query=%20"
	})
	void rejectsInvalidParameters(String queryString) throws Exception {
		mockMvc.perform(get(URI.create("/api/recipes?" + queryString)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

		verifyNoInteractions(recipeService);
	}

	/** Comprueba que la falta de resultados devuelve un 404. */
	@Test
	void mapsRecipeNotFoundToNotFound() throws Exception {
		when(recipeService.search(anyString())).thenThrow(new RecipeNotFoundException("sin resultados"));

		mockMvc.perform(get("/api/recipes").param("query", "xyz"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("sin resultados"));
	}

	/** Comprueba que una clave inválida devuelve un 502. */
	@Test
	void mapsInvalidApiKeyToBadGateway() throws Exception {
		assertUpstreamFailure(Reason.INVALID_API_KEY, 502);
	}

	/** Comprueba que la cuota agotada devuelve un 503. */
	@Test
	void mapsExceededQuotaToServiceUnavailable() throws Exception {
		assertUpstreamFailure(Reason.QUOTA_EXCEEDED, 503);
	}

	/** Comprueba que un error del proveedor devuelve un 502. */
	@Test
	void mapsUpstreamErrorToBadGateway() throws Exception {
		assertUpstreamFailure(Reason.UPSTREAM_ERROR, 502);
	}

	/**
	 * Hace que el servicio falle y comprueba la respuesta de error de la API.
	 *
	 * @param reason motivo del fallo que se simula
	 * @param expectedStatus código HTTP que se espera recibir
	 */
	private void assertUpstreamFailure(Reason reason, int expectedStatus) throws Exception {
		when(recipeService.search(anyString()))
				.thenThrow(new SpoonacularException(reason, "detalle interno"));

		mockMvc.perform(get("/api/recipes").param("query", "pasta"))
				.andExpect(status().is(expectedStatus))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(expectedStatus))
				.andExpect(jsonPath("$.detail").isNotEmpty());
	}
}
