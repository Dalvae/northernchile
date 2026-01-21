package com.northernchile.api.payment.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Payment status enumeration.
 * Represents the lifecycle states of a payment transaction.
 */
public enum PaymentStatus {
    /**
     * Payment has been initiated but not yet processed
     */
    PENDING,

    /**
     * Payment is being processed by the provider
     */
    PROCESSING,

    /**
     * Payment has been successfully completed and confirmed
     */
    COMPLETED,

    /**
     * Payment failed or was rejected
     */
    FAILED,

    /**
     * Payment was cancelled by the user or system
     */
    CANCELLED,

    /**
     * Refund is in progress (transitional state for consistency)
     */
    REFUND_PENDING,

    /**
     * Payment has been refunded (fully or partially)
     */
    REFUNDED,

    /**
     * Payment is expired (e.g., PIX QR code expired)
     */
    EXPIRED;

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
