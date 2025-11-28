package com.northernchile.api.exception;

/**
 * Exception thrown when a payment is declined by the payment provider.
 * This typically happens due to insufficient funds, blocked cards, or bank rejection.
 */
public class PaymentDeclinedException extends PaymentException {

    private final String declineReason;

    public PaymentDeclinedException(String message) {
        super(message, "PAYMENT_DECLINED");
        this.declineReason = null;
    }

    public PaymentDeclinedException(String message, String declineReason) {
        super(message, "PAYMENT_DECLINED");
        this.declineReason = declineReason;
    }

    public PaymentDeclinedException(String message, String declineReason, String providerMessage) {
        super(message, "PAYMENT_DECLINED", providerMessage);
        this.declineReason = declineReason;
    }

    public PaymentDeclinedException(String message, String declineReason, Throwable cause) {
        super(message, "PAYMENT_DECLINED", null, cause);
        this.declineReason = declineReason;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    /**
     * Common decline reason codes for user-friendly messaging
     */
    public static class DeclineReason {
        public static final String INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
        public static final String CARD_EXPIRED = "CARD_EXPIRED";
        public static final String CARD_BLOCKED = "CARD_BLOCKED";
        public static final String INVALID_CARD = "INVALID_CARD";
        public static final String BANK_REJECTED = "BANK_REJECTED";
        public static final String FRAUD_SUSPECTED = "FRAUD_SUSPECTED";
        public static final String LIMIT_EXCEEDED = "LIMIT_EXCEEDED";
        public static final String UNKNOWN = "UNKNOWN";
    }
}
