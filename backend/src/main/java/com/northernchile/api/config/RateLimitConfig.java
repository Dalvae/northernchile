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
 * Provides different rate limits for different endpoint types.
 */
@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> authCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> webhookCache = new ConcurrentHashMap<>();

    /**
     * Get or create an auth bucket for the given IP address.
     * Configuration: 5 requests per minute for authentication endpoints.
     *
     * @param ip The IP address to rate limit
     * @return The bucket for this IP
     */
    public Bucket resolveBucket(String ip) {
        return authCache.computeIfAbsent(ip, k -> createAuthBucket());
    }

    /**
     * Get or create a webhook bucket for the given IP address.
     * Configuration: 30 requests per minute for webhook endpoints.
     *
     * @param ip The IP address to rate limit
     * @return The bucket for this IP
     */
    public Bucket resolveWebhookBucket(String ip) {
        return webhookCache.computeIfAbsent(ip, k -> createWebhookBucket());
    }

    /**
     * Creates a new bucket for auth endpoints.
     * Allows 5 requests per minute with instant refill.
     *
     * @return A new bucket instance
     */
    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Creates a new bucket for webhook endpoints.
     * Allows 30 requests per minute - more permissive for payment callbacks.
     *
     * @return A new bucket instance
     */
    private Bucket createWebhookBucket() {
        Bandwidth limit = Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // ==================== Global Rate Limiting ====================

    private final Map<String, Bucket> globalCache = new ConcurrentHashMap<>();

    /**
     * Get or create a global bucket for the given IP address.
     * Configuration: 100 requests per minute for all API endpoints.
     *
     * @param ip The IP address to rate limit
     * @return The bucket for this IP
     */
    public Bucket resolveGlobalBucket(String ip) {
        return globalCache.computeIfAbsent(ip, k -> createGlobalBucket());
    }

    /**
     * Creates a new bucket for global rate limiting.
     * Allows 100 requests per minute as baseline protection.
     *
     * @return A new bucket instance
     */
    private Bucket createGlobalBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Clear all rate limit caches for a specific IP.
     * Useful for testing or manual override.
     *
     * @param ip The IP address to clear
     */
    public void clearBucket(String ip) {
        authCache.remove(ip);
        webhookCache.remove(ip);
        globalCache.remove(ip);
    }

    /**
     * Clear all rate limit buckets.
     * Useful for testing or system reset.
     */
    public void clearAllBuckets() {
        authCache.clear();
        webhookCache.clear();
        globalCache.clear();
    }
}
