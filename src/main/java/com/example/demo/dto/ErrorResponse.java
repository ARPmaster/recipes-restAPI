package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/**
 * Solo para la documentación OpenAPI: describe el cuerpo real de los errores (RFC 9457)
 * que genera Spring con ProblemDetail. El código no lo instancia.
 *
 * @param title texto corto del estado HTTP
 * @param status código de estado HTTP
 * @param detail explicación del error
 * @param instance ruta de la petición que falló
 */
@Schema(name = "ErrorResponse", description = "Error en formato application/problem+json (RFC 9457)")
public record ErrorResponse(
		@Schema(description = "Texto corto del estado HTTP", example = "Bad Request", requiredMode = RequiredMode.REQUIRED)
		String title,

		@Schema(description = "Código de estado HTTP", example = "400", requiredMode = RequiredMode.REQUIRED)
		int status,

		@Schema(description = "Explicación del error", example = "Required parameter 'query' is not present.",
				requiredMode = RequiredMode.REQUIRED)
		String detail,

		@Schema(description = "Ruta de la petición que falló", example = "/api/recipes", requiredMode = RequiredMode.REQUIRED)
		String instance
) {
}
