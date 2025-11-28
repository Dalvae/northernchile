package com.northernchile.api.exception;

/**
 * Exception thrown when there's an error communicating with the payment provider.
 * This includes network issues, timeouts, or provider service outages.
 */
public class PaymentProviderException extends PaymentException {

    private final String provider;

    public PaymentProviderException(String message, String provider) {
        super(message, "PAYMENT_PROVIDER_ERROR");
        this.provider = provider;
    }

    public PaymentProviderException(String message, String provider, Throwable cause) {
        super(message, "PAYMENT_PROVIDER_ERROR", null, cause);
        this.provider = provider;
    }

    public PaymentProviderException(String message, String provider, String providerMessage, Throwable cause) {
        super(message, "PAYMENT_PROVIDER_ERROR", providerMessage, cause);
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
