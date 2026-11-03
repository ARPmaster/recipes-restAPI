package com.example.demo;

import java.util.List;

/**
 * Respuesta de Spoonacular, separada de los DTOs que expone nuestra API.
 *
 * @param results recetas encontradas; puede ser null o estar vacía
 */
record SpoonacularSearchResponse(
		List<Result> results

) {
	/**
	 * Una receta de los resultados.
	 *
	 * @param id identificador de la receta en Spoonacular
	 * @param title título de la receta
	 * @param image URL de la imagen
	 * @param readyInMinutes tiempo de preparación en minutos
	 * @param servings número de raciones
	 * @param extendedIngredients ingredientes de la receta
	 * @param analyzedInstructions instrucciones, agrupadas en bloques de pasos
	 */
	record Result(
			long id,
			String title,
			String image,
			Integer readyInMinutes,
			Integer servings,
			List<Ingredient> extendedIngredients,
			List<InstructionBlock> analyzedInstructions
	) {
	}

	/**
	 * Un ingrediente.
	 *
	 * @param original texto completo del ingrediente, por ejemplo "2 cups pasta"
	 */
	record Ingredient(String original) {
	}

	/**
	 * Un bloque de instrucciones.
	 *
	 * @param steps pasos del bloque, en orden
	 */
	record InstructionBlock(List<Step> steps) {
	}

	/**
	 * Un paso de una receta.
	 *
	 * @param step texto del paso
	 */
	record Step(String step) {
	}
}