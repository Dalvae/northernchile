package com.northernchile.api.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status values for tour schedules.
 *
 * - OPEN: Schedule is available for booking
 * - CLOSED: Schedule is manually closed for booking
 * - CANCELLED: Schedule has been cancelled
 * - FULL: Schedule has reached maximum capacity
 */
public enum TourScheduleStatus {
    OPEN,
    CLOSED,
    CANCELLED,
    FULL;

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
