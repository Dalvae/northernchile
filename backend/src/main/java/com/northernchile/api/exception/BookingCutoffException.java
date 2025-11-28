package com.northernchile.api.exception;

import java.time.Instant;
import java.util.UUID;

/**
 * Exception thrown when attempting to book a tour after the cutoff time.
 * Returns HTTP 409 Conflict.
 */
public class BookingCutoffException extends BusinessException {

    public static final String ERROR_CODE = "BOOKING_CUTOFF_PASSED";

    private final UUID scheduleId;
    private final Instant cutoffTime;
    private final int hoursRequired;

    public BookingCutoffException(UUID scheduleId, Instant cutoffTime, int hoursRequired) {
        super(ERROR_CODE, String.format(
            "Bookings for this tour are closed. Reservations must be made at least %d hours in advance.",
            hoursRequired));
        this.scheduleId = scheduleId;
        this.cutoffTime = cutoffTime;
        this.hoursRequired = hoursRequired;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public Instant getCutoffTime() {
        return cutoffTime;
    }

    public int getHoursRequired() {
        return hoursRequired;
    }
}
