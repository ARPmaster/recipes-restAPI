package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.SpoonacularSearchResponse.Ingredient;
import com.example.demo.SpoonacularSearchResponse.InstructionBlock;
import com.example.demo.SpoonacularSearchResponse.Result;
import com.example.demo.SpoonacularSearchResponse.Step;
import com.example.demo.dto.RecipeSearchResponse;
import com.example.demo.exception.RecipeNotFoundException;

class RecipeServiceTest {

	private final SpoonacularClient client = mock(SpoonacularClient.class);

	private final RecipeService service = new RecipeService(client);

	/** Comprueba que todos los campos de la receta se copian a la respuesta. */
	@Test
	void mapsAllRecipeFields() {
		givenResults(new Result(1L, "Pasta", "https://img/1.jpg", 45, 4,
				List.of(new Ingredient("1 large onion"), new Ingredient("2 cups pasta")),
				List.of(new InstructionBlock(List.of(new Step("Saute."), new Step("Boil."))))));

		assertThat(service.search("pasta")).isEqualTo(new RecipeSearchResponse(
				"Pasta", 45, 4,
				List.of("1 large onion", "2 cups pasta"),
				List.of("Saute.", "Boil."),
				"https://img/1.jpg"));
	}

	/** Comprueba que, si hay varios resultados, se devuelve solo el primero. */
	@Test
	void returnsOnlyFirstResult() {
		givenResults(
				new Result(1L, "Primera", null, null, null, null, null),
				new Result(2L, "Segunda", null, null, null, null, null));

		assertThat(service.search("pasta").title()).isEqualTo("Primera");
	}

	/** Comprueba que los pasos de todos los bloques se unen en una lista, en orden. */
	@Test
	void joinsStepsFromAllInstructionBlocksInOrder() {
		givenResults(new Result(1L, "Pasta", null, null, null, List.of(), List.of(
				new InstructionBlock(List.of(new Step("Salsa 1"), new Step("Salsa 2"))),
				new InstructionBlock(List.of(new Step("Pasta 1"))))));

		assertThat(service.search("pasta").instructions()).containsExactly("Salsa 1", "Salsa 2", "Pasta 1");
	}

	/** Comprueba que una receta sin pasos devuelve una lista de instrucciones vacía. */
	@Test
	void returnsEmptyInstructionsWhenRecipeHasNone() {
		givenResults(new Result(1L, "Pasta", null, null, null, List.of(), List.of()));

		assertThat(service.search("pasta").instructions()).isEmpty();
	}

	/** Comprueba que la falta de datos opcionales no provoca errores. */
	@Test
	void toleratesMissingOptionalData() {
		givenResults(new Result(1L, "Pasta", null, null, null, null,
				List.of(new InstructionBlock(null))));

		RecipeSearchResponse recipe = service.search("pasta");

		assertThat(recipe.ingredients()).isEmpty();
		assertThat(recipe.instructions()).isEmpty();
		assertThat(recipe.readyInMinutes()).isNull();
		assertThat(recipe.servings()).isNull();
		assertThat(recipe.image()).isNull();
	}

	/** Comprueba que una lista de instrucciones null se trata como una lista vacía. */
	@Test
	void treatsMissingInstructionListAsEmpty() {
		givenResults(new Result(1L, "Pasta", null, null, null, List.of(), null));

		assertThat(service.search("pasta").instructions()).isEmpty();
	}

	/** Comprueba que una lista de resultados vacía lanza RecipeNotFoundException. */
	@Test
	void throwsNotFoundWhenThereAreNoResults() {
		givenResults();

		assertThatThrownBy(() -> service.search("xyz")).isInstanceOf(RecipeNotFoundException.class);
	}

	/** Comprueba que una lista de resultados null lanza RecipeNotFoundException. */
	@Test
	void throwsNotFoundWhenResultsAreMissing() {
		when(client.searchRecipes("xyz")).thenReturn(new SpoonacularSearchResponse(null));

		assertThatThrownBy(() -> service.search("xyz")).isInstanceOf(RecipeNotFoundException.class);
	}

	/**
	 * Simula los resultados que devuelve el cliente de Spoonacular.
	 *
	 * @param results recetas que se devolverán; sin argumentos, la lista queda vacía
	 */
	private void givenResults(Result... results) {
		when(client.searchRecipes(anyString()))
				.thenReturn(new SpoonacularSearchResponse(List.of(results)));
	}
}
