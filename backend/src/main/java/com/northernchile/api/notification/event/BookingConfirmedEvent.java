package com.northernchile.api.notification.event;

import java.util.UUID;

/**
 * Event published when a booking is confirmed.
 * Triggers customer confirmation email and admin notification.
 */
public record BookingConfirmedEvent(
        UUID bookingId,
        String customerEmail,
        String customerName,
        String tourName,
        String tourDate,
        String tourTime,
        int participantCount,
        String totalAmount,
        String languageCode
) {}
