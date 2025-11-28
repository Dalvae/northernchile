package com.northernchile.api.exception;

/**
 * Exception thrown when a payment session expires before completion.
 * This happens when the user doesn't complete payment within the allowed time window.
 */
public class PaymentExpiredException extends PaymentException {

    public PaymentExpiredException(String message) {
        super(message, "PAYMENT_EXPIRED");
    }

    public PaymentExpiredException(String message, Throwable cause) {
        super(message, "PAYMENT_EXPIRED", null, cause);
    }
}
