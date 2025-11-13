package com.northernchile.api.payment.model;

/**
 * Payment method enumeration.
 * Defines the types of payment methods available across different providers.
 */
public enum PaymentMethod {
    /**
     * Webpay Plus - Transbank's credit/debit card payment method (Chile)
     */
    WEBPAY,

    /**
     * PIX - Brazil's instant payment system
     */
    PIX,

    /**
     * Credit card payment (generic)
     */
    CREDIT_CARD,

    /**
     * Debit card payment (generic)
     */
    DEBIT_CARD,

    /**
     * Bank transfer
     */
    BANK_TRANSFER,

    /**
     * Digital wallet (e.g., Mercado Pago wallet)
     */
    WALLET
}
