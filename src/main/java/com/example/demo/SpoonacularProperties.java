package com.example.demo;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spoonacular")
public record SpoonacularProperties(
		@NotBlank String apiKey,
		@NotBlank String baseUrl
) {
}
