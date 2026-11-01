package com.example.demo;

import java.util.List;

/** Respuesta de Spoonacular, separada de los DTOs que expone nuestra API. */
record SpoonacularSearchResponse(
		List<Result> results,
		int offset,
		int number,
		int totalResults
) {

	record Result(long id, String title, String image) {
	}
}
