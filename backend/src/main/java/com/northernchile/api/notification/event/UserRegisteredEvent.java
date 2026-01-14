package com.northernchile.api.notification.event;

/**
 * Event published when a new user registers.
 * Triggers email verification.
 */
public record UserRegisteredEvent(
        String email,
        String fullName,
        String verificationUrl,
        String languageCode
) {}
