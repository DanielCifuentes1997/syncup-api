package com.syncup.syncup_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase de configuración para definir beans globales de la aplicación.
 */
@Configuration
public class AppConfig {

    /**
     * Define un pool de hilos reutilizable para tareas concurrentes.
     * Usaremos un pool de tamaño fijo (ej. 4 hilos) para la búsqueda avanzada.
     * @return Una instancia de ExecutorService gestionada por Spring.
     */
    @Bean
    public ExecutorService taskExecutor() {
        // Crea un pool con un número fijo de hilos (ajustar según necesidad)
        return Executors.newFixedThreadPool(4);
    }
} 