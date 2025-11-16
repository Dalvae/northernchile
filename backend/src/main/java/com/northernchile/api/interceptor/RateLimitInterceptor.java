package com.northernchile.api.interceptor;

import com.northernchile.api.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that enforces rate limiting on authentication endpoints.
 * Uses token bucket algorithm via Bucket4j to prevent brute force attacks.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitConfig rateLimitConfig;

    public RateLimitInterceptor(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIP(request);
        Bucket bucket = rateLimitConfig.resolveBucket(ip);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request allowed - add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            logger.warn("Rate limit exceeded for IP: {} on endpoint: {}", ip, request.getRequestURI());

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\": \"Too many requests\", \"message\": \"Rate limit exceeded. Please try again in %d seconds.\"}",
                waitForRefill
            ));

            return false;
        }
    }

    /**
     * Extract the client's real IP address, considering proxies and load balancers.
     * Checks common proxy headers in order of priority.
     *
     * @param request The HTTP request
     * @return The client's IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty() && !"unknown".equalsIgnoreCase(xfHeader)) {
            // X-Forwarded-For can contain multiple IPs, take the first one (client IP)
            return xfHeader.split(",")[0].trim();
        }

        String xrfHeader = request.getHeader("X-Real-IP");
        if (xrfHeader != null && !xrfHeader.isEmpty() && !"unknown".equalsIgnoreCase(xrfHeader)) {
            return xrfHeader;
        }

        return request.getRemoteAddr();
    }
}
