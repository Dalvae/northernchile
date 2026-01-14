package com.northernchile.api.util;

import com.northernchile.api.config.properties.AppProperties;
import org.springframework.stereotype.Component;

/**
 * Utility class for building frontend URLs with tokens.
 * Centralizes URL construction to avoid duplication and ensure consistency.
 */
@Component
public class UrlBuilder {

    private final AppProperties appProperties;

    public UrlBuilder(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Build email verification URL.
     */
    public String verificationUrl(String token) {
        return appProperties.getFrontendBaseUrl() + "/verify-email?token=" + token;
    }

    /**
     * Build password reset URL.
     */
    public String passwordResetUrl(String token) {
        return appProperties.getFrontendBaseUrl() + "/auth?token=" + token;
    }

    /**
     * Build booking confirmation URL.
     */
    public String bookingConfirmationUrl(String bookingId) {
        return appProperties.getFrontendBaseUrl() + "/bookings/" + bookingId;
    }

    /**
     * Get frontend base URL.
     */
    public String getFrontendBaseUrl() {
        return appProperties.getFrontendBaseUrl();
    }
}
