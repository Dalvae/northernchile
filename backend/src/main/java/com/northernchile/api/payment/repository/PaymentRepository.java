package com.northernchile.api.payment.repository;

import com.northernchile.api.payment.model.Payment;
import com.northernchile.api.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
