package com.pruebaTecnica.BancoCuscatlan.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    private final AppProperties appProperties;

    public CacheConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * El nombre del caché y el TTL se leen desde AppProperties (prefijo "app.cache"),
     * evitando el uso de @Value dispersos.
     * Para habilitar TTL real con caché en memoria se puede migrar a Caffeine:
     *   spring.cache.type=caffeine
     *   spring.cache.caffeine.spec=expireAfterWrite=<ttl>s
     */
    public String getCacheName() {
        return appProperties.getCache().getCacheName();
    }

    public long getCacheTtl() {
        return appProperties.getCache().getTtl();
    }
}
