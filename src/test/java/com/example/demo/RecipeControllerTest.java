package com.example.demo;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.SpoonacularException.Reason;
import com.example.demo.dto.RecipeSearchResponse;
import com.example.demo.dto.RecipeSummary;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RecipeService recipeService;

	@Test
	void returnsRecipes() throws Exception {
		when(recipeService.search("pasta", 2, 4)).thenReturn(new RecipeSearchResponse(
				List.of(new RecipeSummary(1L, "Pasta", "https://img/1.jpg")), 4, 2, 42));

		mockMvc.perform(get("/api/recipes").param("query", "pasta").param("number", "2").param("offset", "4"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.results[0].id").value(1))
				.andExpect(jsonPath("$.results[0].title").value("Pasta"))
				.andExpect(jsonPath("$.results[0].image").value("https://img/1.jpg"))
				.andExpect(jsonPath("$.offset").value(4))
				.andExpect(jsonPath("$.number").value(2))
				.andExpect(jsonPath("$.totalResults").value(42));
	}

	@Test
	void appliesDefaultPagination() throws Exception {
		when(recipeService.search(anyString(), anyInt(), anyInt()))
				.thenReturn(new RecipeSearchResponse(List.of(), 0, 10, 0));

		mockMvc.perform(get("/api/recipes").param("query", "pasta")).andExpect(status().isOk());

		verify(recipeService).search("pasta", 10, 0);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			"query=",
			"query=%20",
			"query=pasta&number=0",
			"query=pasta&number=101",
			"query=pasta&number=abc",
			"query=pasta&offset=-1",
			"query=pasta&offset=901"
	})
	void rejectsInvalidParameters(String queryString) throws Exception {
		mockMvc.perform(get(URI.create("/api/recipes?" + queryString)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

		verifyNoInteractions(recipeService);
	}

	@Test
	void mapsInvalidApiKeyToBadGateway() throws Exception {
		assertUpstreamFailure(Reason.INVALID_API_KEY, 502);
	}

	@Test
	void mapsExceededQuotaToServiceUnavailable() throws Exception {
		assertUpstreamFailure(Reason.QUOTA_EXCEEDED, 503);
	}

	@Test
	void mapsUpstreamErrorToBadGateway() throws Exception {
		assertUpstreamFailure(Reason.UPSTREAM_ERROR, 502);
	}

	private void assertUpstreamFailure(Reason reason, int expectedStatus) throws Exception {
		when(recipeService.search(anyString(), anyInt(), anyInt()))
				.thenThrow(new SpoonacularException(reason, "detalle interno"));

		mockMvc.perform(get("/api/recipes").param("query", "pasta"))
				.andExpect(status().is(expectedStatus))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(expectedStatus))
				.andExpect(jsonPath("$.detail").isNotEmpty());
	}
}
