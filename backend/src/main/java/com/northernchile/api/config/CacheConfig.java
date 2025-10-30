package com.northernchile.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché para WeatherAPI
 * El pronóstico se cachea por 24 horas para evitar llamadas excesivas
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // Cache para pronóstico del tiempo (24 horas)
        ConcurrentMapCache weatherCache = new ConcurrentMapCache("weatherForecast");

        cacheManager.setCaches(Arrays.asList(weatherCache));
        return cacheManager;
    }
}
