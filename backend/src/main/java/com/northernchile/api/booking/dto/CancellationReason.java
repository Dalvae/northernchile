package com.northernchile.api.booking.dto;

/**
 * Reasons for schedule cancellation that triggers cascade refunds.
 */
public enum CancellationReason {
    /**
     * Cancelled due to weather conditions (wind, clouds, etc.)
     */
    WEATHER,

    /**
     * Cancelled due to astronomical conditions (full moon for stargazing tours)
     */
    ASTRONOMICAL,

    /**
     * Admin decision (operational reasons, guide unavailable, etc.)
     */
    ADMIN_DECISION,

    /**
     * Other unspecified reason
     */
    OTHER
}
