package com.northernchile.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status values for tours.
 *
 * - DRAFT: Tour is being created/edited, not visible to public
 * - PUBLISHED: Tour is live and bookable by customers
 */
public enum TourStatus {
    DRAFT,
    PUBLISHED;

    @JsonValue
    public String toValue() {
        return this.name();
    }

    @JsonCreator
    public static TourStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (TourStatus status : TourStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid TourStatus value: " + value);
    }
}
