package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.RecipeSearchResponse;
import jakarta.validation.constraints.NotBlank;

/** Controlador REST que expone la búsqueda de recetas en /api/recipes. */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

	private final RecipeService recipeService;

	/**
	 * Crea el controlador.
	 *
	 * @param recipeService servicio al que se delega la búsqueda de recetas
	 */
	public RecipeController(RecipeService recipeService) {
		this.recipeService = recipeService;
	}

	/**
	 * Atiende GET /api/recipes y devuelve la receta que corresponde a la búsqueda.
	 *
	 * @param query texto a buscar, por ejemplo "pasta"; es obligatorio y no puede estar en blanco
	 * @return la receta encontrada
	 */
	@GetMapping
	public RecipeSearchResponse search(
			@RequestParam @NotBlank String query) {
		return recipeService.search(query);
	}
}
