package com.northernchile.api.exception;

/**
 * Exception thrown when a refund operation fails.
 */
public class RefundException extends PaymentException {

    public RefundException(String message) {
        super(message, "REFUND_FAILED");
    }

    public RefundException(String message, Throwable cause) {
        super(message, "REFUND_FAILED", null, cause);
    }

    public RefundException(String message, String providerMessage, Throwable cause) {
        super(message, "REFUND_FAILED", providerMessage, cause);
    }
}
