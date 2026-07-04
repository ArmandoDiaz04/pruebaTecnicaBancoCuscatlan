package com.pruebaTecnica.BancoCuscatlan.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Configuración de caché básica usando Spring Cache
    // Para configuraciones avanzadas (Redis, Caffeine, etc.) se puede extender aquí
}
