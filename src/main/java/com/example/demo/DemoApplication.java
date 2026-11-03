package com.example.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Clase principal: arranca la aplicación Spring Boot. */
@OpenAPIDefinition(info=@Info(title = "API de Recetas",version = "1.0",description="Busca recetas por nombre en Spoonacular"))
@SpringBootApplication
@ConfigurationPropertiesScan
public class DemoApplication {
	/**
	 * Punto de entrada del programa.
	 *
	 * @param args argumentos recibidos por línea de comandos; se pasan tal cual a Spring
	 */
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
