package com.northernchile.api.notification.event;

/**
 * Event published when a user requests password reset.
 * Triggers password reset email.
 */
public record PasswordResetRequestedEvent(
        String email,
        String fullName,
        String resetUrl,
        String languageCode
) {}
