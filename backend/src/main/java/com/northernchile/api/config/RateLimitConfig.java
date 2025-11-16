package com.northernchile.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j token bucket algorithm.
 * Prevents brute force attacks on authentication endpoints.
 */
@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Get or create a bucket for the given IP address.
     * Configuration: 5 requests per minute for authentication endpoints.
     *
     * @param ip The IP address to rate limit
     * @return The bucket for this IP
     */
    public Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, k -> createNewBucket());
    }

    /**
     * Creates a new bucket with rate limit configuration.
     * Allows 5 requests per minute with instant refill.
     *
     * @return A new bucket instance
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Clear the rate limit cache for a specific IP.
     * Useful for testing or manual override.
     *
     * @param ip The IP address to clear
     */
    public void clearBucket(String ip) {
        cache.remove(ip);
    }

    /**
     * Clear all rate limit buckets.
     * Useful for testing or system reset.
     */
    public void clearAllBuckets() {
        cache.clear();
    }
}
