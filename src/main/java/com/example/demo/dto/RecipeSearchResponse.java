package com.example.demo.dto;

import java.util.List;

/**
 * Receta que devuelve nuestra API al cliente.
 *
 * @param title título de la receta
 * @param readyInMinutes tiempo de preparación en minutos
 * @param servings número de raciones
 * @param ingredients ingredientes, cada uno como un texto
 * @param instructions pasos de la receta, en orden
 * @param image URL de la imagen
 */
public record RecipeSearchResponse(
		String title,
		Integer readyInMinutes,
		Integer servings,
		List<String> ingredients,
		List<String> instructions,
		String image
) {
}

