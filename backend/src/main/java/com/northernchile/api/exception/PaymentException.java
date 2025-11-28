package com.northernchile.api.exception;

/**
 * Base exception for all payment-related errors.
 * This class provides a foundation for more specific payment exceptions.
 */
public class PaymentException extends RuntimeException {

    private final String errorCode;
    private final String providerMessage;

    public PaymentException(String message) {
        super(message);
        this.errorCode = "PAYMENT_ERROR";
        this.providerMessage = null;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PAYMENT_ERROR";
        this.providerMessage = null;
    }

    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.providerMessage = null;
    }

    public PaymentException(String message, String errorCode, String providerMessage) {
        super(message);
        this.errorCode = errorCode;
        this.providerMessage = providerMessage;
    }

    public PaymentException(String message, String errorCode, String providerMessage, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.providerMessage = providerMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getProviderMessage() {
        return providerMessage;
    }
}
