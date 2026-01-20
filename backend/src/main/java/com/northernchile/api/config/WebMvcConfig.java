package com.northernchile.api.config;

import com.northernchile.api.interceptor.GlobalRateLimitInterceptor;
import com.northernchile.api.interceptor.RateLimitInterceptor;
import com.northernchile.api.interceptor.WebhookRateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for interceptors and handlers.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final WebhookRateLimitInterceptor webhookRateLimitInterceptor;
    private final GlobalRateLimitInterceptor globalRateLimitInterceptor;

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor,
                        WebhookRateLimitInterceptor webhookRateLimitInterceptor,
                        GlobalRateLimitInterceptor globalRateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.webhookRateLimitInterceptor = webhookRateLimitInterceptor;
        this.globalRateLimitInterceptor = globalRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Global rate limiting for all API endpoints (100/min)
        // Runs first as baseline protection
        registry.addInterceptor(globalRateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health/**", "/actuator/**")
                .order(0);

        // Stricter rate limiting for authentication endpoints (5/min)
        // Includes login, register, forgot-password, reset-password and check-email to prevent brute force and enumeration
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/forgot-password",
                        "/api/auth/reset-password",
                        "/api/auth/check-email"
                )
                .order(1);

        // Moderate rate limiting for webhook endpoints (30/min)
        registry.addInterceptor(webhookRateLimitInterceptor)
                .addPathPatterns("/api/webhooks/**")
                .order(2);
    }
}
