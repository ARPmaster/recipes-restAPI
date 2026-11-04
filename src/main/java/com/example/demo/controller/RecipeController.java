package com.example.demo.controller;

import com.example.demo.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.RecipeSearchResponse;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Controlador REST que expone la búsqueda de recetas en /api/recipes. */
@Tag(name="Recetas",description="Búsqueda de recetas en Spoonacular")
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
	@Operation(summary = "Buscar una receta por nombre",description = "Devuelve el primer resultado de la búsqueda.")
	@ApiResponse(responseCode = "200", description = "Receta encontrada",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecipeSearchResponse.class)))
	@ApiResponse(responseCode = "400",description = "Falta query o está vacío", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "404",description = "No hay ninguna receta para esta búsqueda", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "502",description = "Spoonacular rechaza la API key o no está disponible", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "503",description = "Cuota de Spoonacular agotada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))

	@GetMapping
	public RecipeSearchResponse search(
			@Parameter(description = "Nombre de la receta",example="pasta")
			@RequestParam @NotBlank String query) {
		return recipeService.search(query);
	}
}
