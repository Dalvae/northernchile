package com.northernchile.api.exception;

import java.util.UUID;

/**
 * Exception thrown when attempting to create a duplicate booking for the same schedule.
 * Returns HTTP 409 Conflict.
 */
public class DuplicateBookingException extends BusinessException {

    public static final String ERROR_CODE = "DUPLICATE_BOOKING";

    private final UUID scheduleId;
    private final UUID existingBookingId;

    public DuplicateBookingException(UUID scheduleId, UUID existingBookingId) {
        super(ERROR_CODE, String.format(
            "You already have a booking for this schedule. Existing booking: %s",
            existingBookingId));
        this.scheduleId = scheduleId;
        this.existingBookingId = existingBookingId;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public UUID getExistingBookingId() {
        return existingBookingId;
    }
}
