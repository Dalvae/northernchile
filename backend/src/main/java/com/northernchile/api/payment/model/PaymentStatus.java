package com.northernchile.api.payment.model;

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
     * Payment has been refunded (fully or partially)
     */
    REFUNDED,

    /**
     * Payment is expired (e.g., PIX QR code expired)
     */
    EXPIRED
}
