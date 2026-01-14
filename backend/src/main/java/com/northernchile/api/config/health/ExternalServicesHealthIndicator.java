package com.northernchile.api.config.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that reports the status of external service connections.
 * Uses circuit breaker states to determine service health.
 */
@Component
public class ExternalServicesHealthIndicator implements HealthIndicator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ExternalServicesHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        boolean anyDown = false;

        // Check weather circuit breaker
        CircuitBreaker weatherCb = circuitBreakerRegistry.find("weather").orElse(null);
        if (weatherCb != null) {
            String state = weatherCb.getState().name();
            builder.withDetail("weather_api", state);
            if (weatherCb.getState() == CircuitBreaker.State.OPEN) {
                anyDown = true;
            }
        } else {
            builder.withDetail("weather_api", "not_configured");
        }

        // Check payment circuit breaker
        CircuitBreaker paymentCb = circuitBreakerRegistry.find("payment").orElse(null);
        if (paymentCb != null) {
            String state = paymentCb.getState().name();
            builder.withDetail("payment_providers", state);
            if (paymentCb.getState() == CircuitBreaker.State.OPEN) {
                anyDown = true;
            }
        } else {
            builder.withDetail("payment_providers", "not_configured");
        }

        // If any circuit breaker is open, report degraded status
        if (anyDown) {
            return builder.status("DEGRADED").build();
        }

        return builder.build();
    }
}
