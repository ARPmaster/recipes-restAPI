package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.RecipeSearchResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

	private final RecipeService recipeService;

	public RecipeController(RecipeService recipeService) {
		this.recipeService = recipeService;
	}

	@GetMapping
	public RecipeSearchResponse search(
			@RequestParam @NotBlank String query,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int number,
			@RequestParam(defaultValue = "0") @Min(0) @Max(900) int offset) {
		return recipeService.search(query, number, offset);
	}
}
