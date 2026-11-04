package com.example.demo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/** Activa la caché de Spring. Los ajustes (tamaño y caducidad) están en application.yml.Se puede usar para crear chae managers personalizados
 *  */
@EnableCaching
@Configuration
public class CacheConfig {

}
