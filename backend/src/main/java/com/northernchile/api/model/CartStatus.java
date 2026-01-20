package com.northernchile.api.model;

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
    CONVERTED
}
