package com.northernchile.api.notification.event;

import java.util.UUID;

/**
 * Event published when a contact message is received.
 * Triggers admin notification.
 */
public record ContactMessageReceivedEvent(
        UUID messageId,
        String name,
        String email,
        String phone,
        String message
) {}
