package com.northernchile.api.payment;

import com.northernchile.api.payment.dto.PaymentInitReq;
import com.northernchile.api.payment.dto.PaymentInitRes;
import com.northernchile.api.payment.dto.PaymentStatusRes;
import com.northernchile.api.payment.model.Payment;
import com.northernchile.api.payment.provider.PaymentProviderFactory;
import com.northernchile.api.payment.provider.PaymentProviderService;
import com.northernchile.api.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Payment service facade.
 * Orchestrates payment operations and delegates to specific payment provider services.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentProviderFactory providerFactory;

    public PaymentService(PaymentRepository paymentRepository, PaymentProviderFactory providerFactory) {
        this.paymentRepository = paymentRepository;
        this.providerFactory = providerFactory;
    }

    /**
     * Create a new payment transaction.
     *
     * @param request Payment initialization request
     * @return Payment initialization response
     */
    @Transactional
    public PaymentInitRes createPayment(PaymentInitReq request) {
        log.info("Creating payment for booking: {} with provider: {}",
            request.getBookingId(), request.getProvider());

        // Validate request
        validatePaymentRequest(request);

        // Get the appropriate payment provider
        PaymentProviderService provider = providerFactory.getProvider(request.getProvider());

        // Delegate to provider
        PaymentInitRes response = provider.createPayment(request);

        log.info("Payment created successfully: {}", response.getPaymentId());
        return response;
    }

    /**
     * Confirm a payment (for redirect-based flows like Webpay).
     *
     * @param token Payment token
     * @return Payment status
     */
    @Transactional
    public PaymentStatusRes confirmPayment(String token) {
        log.info("Confirming payment with token: {}", token);

        // Find payment by token
        Payment payment = paymentRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found for token: " + token));

        // Get the appropriate payment provider
        PaymentProviderService provider = providerFactory.getProvider(payment.getProvider());

        // Delegate to provider
        PaymentStatusRes response = provider.confirmPayment(token);

        log.info("Payment confirmed: {} with status: {}", response.getPaymentId(), response.getStatus());
        return response;
    }

    /**
     * Get payment status by payment ID.
     *
     * @param paymentId Internal payment ID
     * @return Payment status
     */
    public PaymentStatusRes getPaymentStatus(UUID paymentId) {
        log.info("Getting payment status: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        PaymentStatusRes response = new PaymentStatusRes();
        response.setPaymentId(payment.getId());
        response.setExternalPaymentId(payment.getExternalPaymentId());
        response.setStatus(payment.getStatus());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setUpdatedAt(payment.getUpdatedAt());

        return response;
    }

    /**
     * Get payment status from provider by external payment ID.
     *
     * @param externalPaymentId Provider's payment ID
     * @return Payment status
     */
    public PaymentStatusRes getPaymentStatusFromProvider(String externalPaymentId) {
        log.info("Getting payment status from provider: {}", externalPaymentId);

        Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + externalPaymentId));

        // Get the appropriate payment provider
        PaymentProviderService provider = providerFactory.getProvider(payment.getProvider());

        // Delegate to provider
        return provider.getPaymentStatus(externalPaymentId);
    }

    /**
     * Get all payments for a booking.
     *
     * @param bookingId Booking ID
     * @return List of payments
     */
    public List<Payment> getBookingPayments(UUID bookingId) {
        log.info("Getting payments for booking: {}", bookingId);
        return paymentRepository.findByBookingId(bookingId);
    }

    /**
     * Refund a payment.
     *
     * @param paymentId Payment ID
     * @param amount Amount to refund (null for full refund)
     * @return Payment status after refund
     */
    @Transactional
    public PaymentStatusRes refundPayment(UUID paymentId, BigDecimal amount) {
        log.info("Refunding payment: {} with amount: {}", paymentId, amount);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        // Validate payment can be refunded
        if (!canRefund(payment)) {
            throw new IllegalStateException("Payment cannot be refunded in current state: " + payment.getStatus());
        }

        // Get the appropriate payment provider
        PaymentProviderService provider = providerFactory.getProvider(payment.getProvider());

        // Delegate to provider
        PaymentStatusRes response = provider.refundPayment(payment, amount);

        log.info("Payment refunded: {} with status: {}", response.getPaymentId(), response.getStatus());
        return response;
    }

    /**
     * Process a webhook notification.
     *
     * @param provider Payment provider
     * @param payload Webhook payload
     * @return Payment status after processing
     */
    @Transactional
    public PaymentStatusRes processWebhook(String provider, Map<String, Object> payload) {
        log.info("Processing webhook for provider: {}", provider);

        try {
            // Get the appropriate payment provider
            PaymentProviderService providerService = providerFactory.getProvider(
                com.northernchile.api.payment.model.PaymentProvider.valueOf(provider.toUpperCase())
            );

            // Delegate to provider
            return providerService.processWebhook(payload);
        } catch (IllegalArgumentException e) {
            log.error("Unsupported payment provider in webhook: {}", provider, e);
            throw new IllegalArgumentException("Unsupported payment provider: " + provider, e);
        }
    }

    /**
     * Validate payment request.
     */
    private void validatePaymentRequest(PaymentInitReq request) {
        if (request.getBookingId() == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        if (request.getProvider() == null) {
            throw new IllegalArgumentException("Payment provider is required");
        }
        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    /**
     * Check if a payment can be refunded.
     */
    private boolean canRefund(Payment payment) {
        return switch (payment.getStatus()) {
            case COMPLETED -> true;
            case PENDING, PROCESSING, FAILED, CANCELLED, REFUNDED, EXPIRED -> false;
        };
    }
}
