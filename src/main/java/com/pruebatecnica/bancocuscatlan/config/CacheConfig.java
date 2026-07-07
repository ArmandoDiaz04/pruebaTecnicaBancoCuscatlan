package com.pruebatecnica.bancocuscatlan.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Único punto de verdad del nombre de caché, consumido por ReportService
     * (@Cacheable) y ReportCacheListener (@CacheEvict) para evitar el drift
     * entre configuración y literales.
     */
    public static final String OCCUPANCY_REPORT_CACHE = "occupancyReport";

    private final AppProperties appProperties;

    public CacheConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(OCCUPANCY_REPORT_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(appProperties.getCache().getTtl()))
                .maximumSize(500));
        return cacheManager;
    }
}
