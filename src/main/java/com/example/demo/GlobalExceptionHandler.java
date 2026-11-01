package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** La clase base convierte los errores de validación de parámetros en un 400 con ProblemDetail. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
}
