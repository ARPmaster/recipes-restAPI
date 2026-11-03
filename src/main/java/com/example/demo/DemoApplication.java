package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Clase principal: arranca la aplicación Spring Boot. */
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
