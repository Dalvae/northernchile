package com.northernchile.api.payment.model;

/**
 * Payment provider enumeration.
 * Defines the available payment gateway providers.
 */
public enum PaymentProvider {
    /**
     * Transbank - Chile's primary payment processor (Webpay Plus)
     */
    TRANSBANK,

    /**
     * Mercado Pago - Latin America payment processor (supports PIX, credit cards, etc.)
     */
    MERCADOPAGO,

    /**
     * Stripe - International payment processor (future implementation)
     */
    STRIPE
}
