package com.example.demo.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/**
 * Receta que devuelve nuestra API al cliente.
 * Todas las claves se envían siempre; las listas nunca son null, pero los demás campos
 * pueden llegar a null si Spoonacular no los informa.
 *
 * @param title título de la receta
 * @param readyInMinutes tiempo de preparación en minutos
 * @param servings número de raciones
 * @param ingredients ingredientes, cada uno como un texto
 * @param instructions pasos de la receta, en orden
 * @param image URL de la imagen
 */
@Schema(description = "Receta encontrada. Todas las claves se envían siempre; las listas pueden estar vacías pero nunca son null.")
public record RecipeSearchResponse(
		@Schema(description = "Título de la receta", example = "Pasta con tomate",
				requiredMode = RequiredMode.REQUIRED, types = {"string", "null"})
		String title,

		@Schema(description = "Tiempo de preparación en minutos", example = "30",
				requiredMode = RequiredMode.REQUIRED, types = {"integer", "null"}, format = "int32")
		Integer readyInMinutes,

		@Schema(description = "Número de raciones", example = "4",
				requiredMode = RequiredMode.REQUIRED, types = {"integer", "null"}, format = "int32")
		Integer servings,

		@ArraySchema(arraySchema = @Schema(description = "Ingredientes de la receta; lista vacía si no hay",
				requiredMode = RequiredMode.REQUIRED),
				schema = @Schema(example = "400 g de pasta"))
		List<String> ingredients,

		@ArraySchema(arraySchema = @Schema(description = "Pasos de la receta, en orden; lista vacía si no hay",
				requiredMode = RequiredMode.REQUIRED),
				schema = @Schema(example = "Cuece la pasta."))
		List<String> instructions,

		@Schema(description = "URL de la imagen", example = "https://img.spoonacular.com/recipes/652332-312x231.jpg",
				requiredMode = RequiredMode.REQUIRED, types = {"string", "null"}, format = "uri")
		String image
) {
}
