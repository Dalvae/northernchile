package com.northernchile.api.payment.repository;

import com.northernchile.api.payment.model.Payment;
import com.northernchile.api.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Find payment by external payment ID (provider's transaction ID)
     */
    Optional<Payment> findByExternalPaymentId(String externalPaymentId);

    /**
     * Find all payments for a specific booking
     */
    List<Payment> findByBookingId(UUID bookingId);

    /**
     * Find all payments for a booking with a specific status
     */
    List<Payment> findByBookingIdAndStatus(UUID bookingId, PaymentStatus status);

    /**
     * Find payment by token (e.g., Webpay token)
     */
    Optional<Payment> findByToken(String token);

    /**
     * Find all test payments (for cleanup/filtering)
     */
    List<Payment> findByIsTest(boolean isTest);

    /**
     * Find all real (non-test) payments
     */
    default List<Payment> findRealPayments() {
        return findByIsTest(false);
    }

    /**
     * Delete all test payments (admin only - for cleanup)
     */
    void deleteByIsTest(boolean isTest);

    /**
     * Check if there's an active (pending or processing) payment for a booking.
     * Used for idempotency to prevent duplicate payment attempts.
     */
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId " +
           "AND p.status IN (com.northernchile.api.payment.model.PaymentStatus.PENDING, " +
           "com.northernchile.api.payment.model.PaymentStatus.PROCESSING) " +
           "AND (p.expiresAt IS NULL OR p.expiresAt > :now)")
    Optional<Payment> findActivePaymentForBooking(@Param("bookingId") UUID bookingId, @Param("now") Instant now);

    /**
     * Find payment by idempotency key
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find completed payments within a date range (for financial reports).
     * Excludes test payments.
     */
    @Query("SELECT p FROM Payment p WHERE p.status = com.northernchile.api.payment.model.PaymentStatus.COMPLETED " +
           "AND p.isTest = false " +
           "AND p.createdAt >= :start AND p.createdAt < :end")
    List<Payment> findCompletedBetween(@Param("start") Instant start, @Param("end") Instant end);
}
