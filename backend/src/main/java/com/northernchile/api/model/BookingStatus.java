package com.northernchile.api.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status values for bookings.
 *
 * Valid transitions:
 * - PENDING -> CONFIRMED (after payment)
 * - PENDING -> CANCELLED (before 24h)
 * - CONFIRMED -> COMPLETED (after tour date)
 * - CONFIRMED -> CANCELLED (before 24h, with refund)
 * - CANCELLED -> (no transitions allowed)
 * - COMPLETED -> (no transitions allowed)
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    REFUNDED;

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
