package com.example.demo;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.example.demo.exception.RecipeNotFoundException;

import com.example.demo.dto.RecipeSearchResponse;

/** Lógica de negocio: busca recetas y las convierte al formato que devuelve nuestra API. */
@Service
public class RecipeService {

	private final SpoonacularClient client;

	/**
	 * Crea el servicio.
	 *
	 * @param client cliente con el que se consulta Spoonacular
	 */
	public RecipeService(SpoonacularClient client) {
		this.client = client;
	}

	/**
	 * Busca una receta y devuelve el primer resultado.
	 * Las búsquedas correctas se guardan en caché, sin distinguir mayúsculas ni espacios en los extremos.
	 * Si la búsqueda falla, no se guarda nada y el siguiente intento vuelve a llamar a Spoonacular.
	 *
	 * @param query texto a buscar
	 * @return la receta con su título, tiempo, raciones, ingredientes, pasos e imagen
	 * @throws RecipeNotFoundException si la búsqueda no da ningún resultado
	 */
	@Cacheable(cacheNames = "recipes",key = "#query.trim().toLowerCase()")
	public RecipeSearchResponse search(String query) {
		SpoonacularSearchResponse response = client.searchRecipes(query);
		if(response.results()==null||response.results().isEmpty()){
			throw new RecipeNotFoundException("No se ha encontrado ninguna receta para la búsqueda indicada,");
		}
		SpoonacularSearchResponse.Result recipe=response.results().getFirst();
		List<String> ingredientes=orEmpty(recipe.extendedIngredients()).stream().map(SpoonacularSearchResponse.Ingredient::original).toList();
		List<String> instructions=orEmpty(recipe.analyzedInstructions()).stream().flatMap(block->orEmpty(block.steps()).stream()).map(SpoonacularSearchResponse.Step::step).toList();

		return new RecipeSearchResponse(recipe.title(),recipe.readyInMinutes(),recipe.servings(),ingredientes,instructions,recipe.image());
	}
	/**
	 * Evita los null: Spoonacular puede no enviar algunas listas.
	 *
	 * @param list lista a comprobar; puede ser null
	 * @param <T> tipo de los elementos de la lista
	 * @return la misma lista o, si es null, una lista vacía
	 */
	private static <T> List<T> orEmpty(List<T> list){
		return list==null?List.of():list;
	}
}
