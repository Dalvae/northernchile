package com.northernchile.api.exception;

import java.util.UUID;

/**
 * Exception thrown when a tour schedule has no available slots for booking.
 * Returns HTTP 409 Conflict.
 */
public class ScheduleFullException extends BusinessException {

    public static final String ERROR_CODE = "SCHEDULE_FULL";

    private final UUID scheduleId;
    private final int requestedSlots;
    private final int availableSlots;

    public ScheduleFullException(UUID scheduleId, int requestedSlots, int availableSlots) {
        super(ERROR_CODE, String.format(
            "Not enough availability for schedule %s. Requested: %d, Available: %d",
            scheduleId, requestedSlots, availableSlots));
        this.scheduleId = scheduleId;
        this.requestedSlots = requestedSlots;
        this.availableSlots = availableSlots;
    }

    public ScheduleFullException(String message) {
        super(ERROR_CODE, message);
        this.scheduleId = null;
        this.requestedSlots = 0;
        this.availableSlots = 0;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public int getRequestedSlots() {
        return requestedSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }
}
