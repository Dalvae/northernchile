package com.northernchile.api.exception;

import java.util.UUID;

/**
 * Exception thrown when attempting to book an inactive or unpublished tour.
 * Returns HTTP 409 Conflict.
 */
public class TourNotActiveException extends BusinessException {

    public static final String ERROR_CODE = "TOUR_NOT_ACTIVE";

    private final UUID tourId;

    public TourNotActiveException(UUID tourId) {
        super(ERROR_CODE, String.format("Tour %s is not active or published", tourId));
        this.tourId = tourId;
    }

    public TourNotActiveException(UUID tourId, String reason) {
        super(ERROR_CODE, String.format("Tour %s is not available: %s", tourId, reason));
        this.tourId = tourId;
    }

    public UUID getTourId() {
        return tourId;
    }
}
