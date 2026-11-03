package com.example.demo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** La clase base convierte los errores de validación de parámetros en un 400 con ProblemDetail. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * Registra el fallo en el log y lo convierte en una respuesta de error sin detalles internos.
	 *
	 * @param ex excepción lanzada; su motivo decide la respuesta
	 * @return 503 si se ha agotado la cuota y 502 en los demás casos
	 */
	@ExceptionHandler(SpoonacularException.class)
	public ProblemDetail handleSpoonacular(SpoonacularException ex) {
		log.warn("Fallo al llamar a Spoonacular ({}): {}", ex.getReason(), ex.getMessage());
		return switch (ex.getReason()) {
			case INVALID_API_KEY -> ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
					"El proveedor de recetas ha rechazado la autenticación del servicio.");
			case QUOTA_EXCEEDED -> ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
					"Se ha agotado temporalmente la cuota del proveedor de recetas. Inténtalo más tarde.");
			case UPSTREAM_ERROR -> ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
					"El proveedor de recetas no está disponible.");
		};
	}
	/**
	 * Convierte la falta de resultados en una respuesta de error.
	 *
	 * @param ex excepción lanzada; su mensaje se envía al cliente
	 * @return respuesta con estado 404
	 */
	@ExceptionHandler(RecipeNotFoundException.class)
	public ProblemDetail handleNotFound(RecipeNotFoundException ex){
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,ex.getMessage());
	}
}
