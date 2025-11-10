package com.northernchile.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for improved performance.
 * - weatherForecast: 24-hour cache for weather API calls
 * - tour-detail: 30-minute cache for individual tour pages
 * - tour-list: 15-minute cache for tour lists
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // Weather forecast cache (24 hours)
        CaffeineCache weatherCache = buildCache("weatherForecast", 1440, 100);

        // Tour detail cache (30 minutes, by slug + locale)
        CaffeineCache tourDetailCache = buildCache("tour-detail", 30, 500);

        // Tour list cache (15 minutes)
        CaffeineCache tourListCache = buildCache("tour-list", 15, 50);

        cacheManager.setCaches(Arrays.asList(weatherCache, tourDetailCache, tourListCache));
        return cacheManager;
    }

    /**
     * Builds a Caffeine cache with specified expiration and size.
     *
     * @param name Cache name
     * @param minutesToExpire Minutes until cache entries expire
     * @param maxSize Maximum number of entries in cache
     * @return Configured CaffeineCache
     */
    private CaffeineCache buildCache(String name, long minutesToExpire, long maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(minutesToExpire, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .recordStats()
                .build());
    }
}
