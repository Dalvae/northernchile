package com.northernchile.api.payment;

import com.northernchile.api.payment.dto.PaymentRes;
import com.northernchile.api.payment.dto.PaymentStatusRes;
import com.northernchile.api.payment.model.Payment;
import com.northernchile.api.payment.model.PaymentStatus;
import com.northernchile.api.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Payment service for managing Payment entities.
 * Note: Primary payment flow now uses PaymentSessionService.
 * This service handles legacy Payment entities, refunds, and admin operations.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Get payment status by payment ID.
     */
    public PaymentStatusRes getPaymentStatus(UUID paymentId) {
        log.info("Getting payment status: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        return buildStatusResponse(payment);
    }

    /**
     * Get all payments for a booking.
     */
    public List<PaymentRes> getBookingPayments(UUID bookingId) {
        log.info("Getting payments for booking: {}", bookingId);
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(this::toPaymentRes)
                .collect(Collectors.toList());
    }

    /**
     * Refund a payment.
     * Note: This is a simplified version - actual provider refund logic
     * should be implemented based on the provider (Transbank/MercadoPago).
     */
    @Transactional
    public PaymentStatusRes refundPayment(UUID paymentId, BigDecimal amount) {
        log.info("Refunding payment: {} with amount: {}", paymentId, amount);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        // Validate payment can be refunded
        validateRefund(payment);

        // Mark as refunded (actual provider call would go here)
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        log.info("Payment {} marked as refunded", paymentId);
        return buildStatusResponse(payment);
    }

    /**
     * Get all test payments (for admin review/cleanup).
     */
    public List<PaymentRes> getTestPayments() {
        log.info("Retrieving all test payments");
        return paymentRepository.findByIsTest(true).stream()
                .map(this::toPaymentRes)
                .collect(Collectors.toList());
    }

    /**
     * Delete all test payments (super admin only).
     */
    @Transactional
    public int deleteTestPayments() {
        log.warn("Deleting all test payments - this action cannot be undone");

        List<Payment> testPayments = paymentRepository.findByIsTest(true);
        int count = testPayments.size();

        if (count == 0) {
            log.info("No test payments found to delete");
            return 0;
        }

        paymentRepository.deleteByIsTest(true);
        log.warn("Deleted {} test payment(s)", count);

        return count;
    }

    private PaymentStatusRes buildStatusResponse(Payment payment) {
        return new PaymentStatusRes(
            payment.getId(),
            payment.getExternalPaymentId(),
            payment.getStatus(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getErrorMessage(),
            payment.getUpdatedAt(),
            payment.isTest()
        );
    }

    private PaymentRes toPaymentRes(Payment payment) {
        return new PaymentRes(
            payment.getId(),
            payment.getBooking() != null ? payment.getBooking().getId() : null,
            payment.getPaymentSession() != null ? payment.getPaymentSession().getId() : null,
            payment.getProvider(),
            payment.getPaymentMethod(),
            payment.getExternalPaymentId(),
            payment.getStatus(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getPaymentUrl(),
            payment.getDetailsUrl(),
            payment.getQrCode(),
            payment.getPixCode(),
            payment.getToken(),
            payment.getExpiresAt(),
            payment.getErrorMessage(),
            payment.isTest(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }

    private void validateRefund(Payment payment) {
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot refund payment - status is " + payment.getStatus());
        }

        // Check 24-hour cancellation policy
        if (payment.getBooking() == null || payment.getBooking().getSchedule() == null
                || payment.getBooking().getSchedule().getStartDatetime() == null) {
            throw new IllegalStateException("Cannot process refund - tour schedule information is missing");
        }

        Instant tourStart = payment.getBooking().getSchedule().getStartDatetime();
        long hoursUntilTour = ChronoUnit.HOURS.between(Instant.now(), tourStart);

        if (hoursUntilTour < 24) {
            throw new IllegalStateException(
                String.format("Cannot refund less than 24 hours before tour. Hours remaining: %d", hoursUntilTour)
            );
        }
    }
}
