package com.example.demo;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/** Activa la caché de Spring. Los ajustes (tamaño y caducidad) están en application.yml. */
@EnableCaching
@Configuration
public class CacheConfig {

}
