package com.northernchile.api.config;

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

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor,
                        WebhookRateLimitInterceptor webhookRateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.webhookRateLimitInterceptor = webhookRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply rate limiting to authentication endpoints (5/min)
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/auth/login", "/api/auth/register")
                .order(1);

        // Apply rate limiting to webhook endpoints (30/min)
        registry.addInterceptor(webhookRateLimitInterceptor)
                .addPathPatterns("/api/webhooks/**")
                .order(2);
    }
}
