package com.northernchile.api.exception;

import java.util.List;
import java.util.UUID;

/**
 * Exception thrown when a booking state transition is invalid.
 * Returns HTTP 409 Conflict.
 */
public class InvalidBookingStateException extends BusinessException {

    public static final String ERROR_CODE = "INVALID_BOOKING_STATE";

    private final UUID bookingId;
    private final String currentStatus;
    private final String requestedStatus;
    private final List<String> allowedTransitions;

    public InvalidBookingStateException(UUID bookingId, String currentStatus, String requestedStatus, List<String> allowedTransitions) {
        super(ERROR_CODE, String.format(
            "Invalid status transition from %s to %s. Allowed transitions: %s",
            currentStatus, requestedStatus, 
            allowedTransitions.isEmpty() ? "none" : String.join(", ", allowedTransitions)));
        this.bookingId = bookingId;
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
        this.allowedTransitions = allowedTransitions;
    }

    public InvalidBookingStateException(String message) {
        super(ERROR_CODE, message);
        this.bookingId = null;
        this.currentStatus = null;
        this.requestedStatus = null;
        this.allowedTransitions = List.of();
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getRequestedStatus() {
        return requestedStatus;
    }

    public List<String> getAllowedTransitions() {
        return allowedTransitions;
    }
}
