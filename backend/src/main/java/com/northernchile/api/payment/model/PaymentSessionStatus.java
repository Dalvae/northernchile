package com.northernchile.api.payment.model;

/**
 * Status of a payment session.
 */
public enum PaymentSessionStatus {
    /**
     * Session created, waiting for user to complete payment
     */
    PENDING,

    /**
     * Payment completed successfully, bookings created
     */
    COMPLETED,

    /**
     * Payment failed
     */
    FAILED,

    /**
     * Session expired (30 min timeout)
     */
    EXPIRED,

    /**
     * User cancelled the payment
     */
    CANCELLED,

    /**
     * Payment was refunded
     */
    REFUNDED
}
