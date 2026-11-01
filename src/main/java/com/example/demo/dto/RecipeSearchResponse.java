package com.example.demo.dto;

import java.util.List;

public record RecipeSearchResponse(
		List<RecipeSummary> results,
		int offset,
		int number,
		int totalResults
) {
}
