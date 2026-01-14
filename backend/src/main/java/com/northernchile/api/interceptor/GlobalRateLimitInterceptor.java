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
 * Global rate limiting interceptor for all API endpoints.
 * Provides baseline protection (100 requests/minute per IP) against abuse.
 */
@Component
public class GlobalRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(GlobalRateLimitInterceptor.class);

    private final RateLimitConfig rateLimitConfig;

    public GlobalRateLimitInterceptor(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIP(request);
        Bucket bucket = rateLimitConfig.resolveGlobalBucket(ip);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            logger.warn("Global rate limit exceeded for IP: {} on endpoint: {}", ip, request.getRequestURI());

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
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty() && !"unknown".equalsIgnoreCase(xfHeader)) {
            return xfHeader.split(",")[0].trim();
        }

        String xrfHeader = request.getHeader("X-Real-IP");
        if (xrfHeader != null && !xrfHeader.isEmpty() && !"unknown".equalsIgnoreCase(xrfHeader)) {
            return xrfHeader;
        }

        return request.getRemoteAddr();
    }
}
