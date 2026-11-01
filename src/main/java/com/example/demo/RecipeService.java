package com.example.demo;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.RecipeSearchResponse;
import com.example.demo.dto.RecipeSummary;

@Service
public class RecipeService {

	private final SpoonacularClient client;

	public RecipeService(SpoonacularClient client) {
		this.client = client;
	}

	public RecipeSearchResponse search(String query, int number, int offset) {
		SpoonacularSearchResponse response = client.searchRecipes(query, number, offset);
		List<RecipeSummary> results = response.results() == null
				? List.of()
				: response.results().stream()
						.map(recipe -> new RecipeSummary(recipe.id(), recipe.title(), recipe.image()))
						.toList();
		return new RecipeSearchResponse(results, response.offset(), response.number(), response.totalResults());
	}
}
