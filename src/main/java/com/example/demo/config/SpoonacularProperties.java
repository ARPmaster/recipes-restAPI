package com.example.demo.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración de Spoonacular, leída de las propiedades con prefijo "spoonacular".
 * La aplicación no arranca si alguno de los dos valores está vacío. Si la variable
 * SPOONACULAR_API_KEY no está definida, sí arranca, y el fallo no se detecta en ese momento.
 *
 * @param apiKey clave con la que nos autenticamos en Spoonacular
 * @param baseUrl dirección base de la API de Spoonacular
 */
@Validated
@ConfigurationProperties(prefix = "spoonacular")
public record SpoonacularProperties(
		@NotBlank String apiKey,
		@NotBlank String baseUrl
) {
}
