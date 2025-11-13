package com.northernchile.api.payment.provider;

import com.northernchile.api.payment.dto.PaymentInitReq;
import com.northernchile.api.payment.dto.PaymentInitRes;
import com.northernchile.api.payment.dto.PaymentStatusRes;
import com.northernchile.api.payment.model.Payment;

/**
 * Payment provider service interface.
 * Defines the contract for all payment provider implementations (Transbank, Mercado Pago, Stripe, etc.).
 * Uses the Strategy pattern to allow different payment providers to be used interchangeably.
 */
public interface PaymentProviderService {

    /**
     * Initialize a payment transaction with the provider.
     *
     * @param request Payment initialization request
     * @return Payment initialization response with payment URL, tokens, etc.
     */
    PaymentInitRes createPayment(PaymentInitReq request);

    /**
     * Confirm a payment transaction.
     * Used for providers that require explicit confirmation (e.g., Webpay after redirect).
     *
     * @param token Payment token from the provider
     * @return Updated payment status
     */
    PaymentStatusRes confirmPayment(String token);

    /**
     * Get the current status of a payment from the provider.
     *
     * @param externalPaymentId Provider's payment ID
     * @return Current payment status
     */
    PaymentStatusRes getPaymentStatus(String externalPaymentId);

    /**
     * Refund a payment.
     *
     * @param payment Payment to refund
     * @param amount Amount to refund (null for full refund)
     * @return Refund status
     */
    PaymentStatusRes refundPayment(Payment payment, java.math.BigDecimal amount);

    /**
     * Process a webhook notification from the payment provider.
     *
     * @param payload Webhook payload
     * @return Payment status after processing webhook
     */
    PaymentStatusRes processWebhook(java.util.Map<String, Object> payload);
}
