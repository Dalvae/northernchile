package com.northernchile.api.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status values for shopping carts.
 *
 * - ACTIVE: Cart is active and can be modified
 * - EXPIRED: Cart has expired due to timeout
 * - CONVERTED: Cart has been converted to booking(s)
 */
public enum CartStatus {
    ACTIVE,
    EXPIRED,
    CONVERTED;

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
