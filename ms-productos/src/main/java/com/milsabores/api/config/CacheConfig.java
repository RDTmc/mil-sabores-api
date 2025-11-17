package com.milsabores.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Caché simple en memoria (sin TTL). Nombres declarados para visibilidad.
        return new ConcurrentMapCacheManager("categories", "featured");
    }
}
