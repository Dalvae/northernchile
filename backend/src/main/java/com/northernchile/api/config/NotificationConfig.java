package com.northernchile.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized configuration for notification settings.
 * Provides admin email and other notification-related properties.
 */
@Configuration
public class NotificationConfig {

    @Value("${notification.admin.email}")
    private String adminEmail;

    /**
     * Get the admin email address for notifications.
     * This email receives booking alerts, contact forms, private tour requests, etc.
     */
    public String getAdminEmail() {
        return adminEmail;
    }
}
