package com.northernchile.api.payment.repository;

import com.northernchile.api.payment.model.PaymentSession;
import com.northernchile.api.payment.model.PaymentSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentSessionRepository extends JpaRepository<PaymentSession, UUID> {

    Optional<PaymentSession> findByToken(String token);

    Optional<PaymentSession> findByExternalPaymentId(String externalPaymentId);

    List<PaymentSession> findByUserIdAndStatus(UUID userId, PaymentSessionStatus status);

    /**
     * Find expired pending sessions (for cleanup job)
     */
    @Query("SELECT ps FROM PaymentSession ps WHERE ps.status = 'PENDING' AND ps.expiresAt < :now")
    List<PaymentSession> findExpiredPendingSessions(@Param("now") Instant now);

    /**
     * Count reserved slots for a schedule from pending payment sessions.
     * Used for availability calculation.
     */
    @Query(value = """
        SELECT COALESCE(SUM(
            (SELECT SUM((item->>'numParticipants')::int)
             FROM jsonb_array_elements(ps.items) AS item
             WHERE (item->>'scheduleId')::uuid = :scheduleId)
        ), 0)
        FROM payment_sessions ps
        WHERE ps.status = 'PENDING'
          AND ps.expires_at > :now
        """, nativeQuery = true)
    int countReservedSlotsByScheduleId(@Param("scheduleId") UUID scheduleId, @Param("now") Instant now);

    /**
     * Expire old pending sessions
     */
    @Modifying
    @Query("UPDATE PaymentSession ps SET ps.status = 'EXPIRED' WHERE ps.status = 'PENDING' AND ps.expiresAt < :now")
    int expirePendingSessions(@Param("now") Instant now);

    /**
     * Check if there are any expired pending sessions (lightweight check before running UPDATE)
     */
    @Query("SELECT CASE WHEN COUNT(ps) > 0 THEN true ELSE false END FROM PaymentSession ps WHERE ps.status = 'PENDING' AND ps.expiresAt < :now")
    boolean existsExpiredPendingSessions(@Param("now") Instant now);

    /**
     * Find a completed payment session for a user that contains a specific schedule.
     * Used for refund processing.
     */
    @Query(value = """
        SELECT ps.* FROM payment_sessions ps
        WHERE ps.user_id = :userId
          AND ps.status = 'COMPLETED'
          AND EXISTS (
            SELECT 1 FROM jsonb_array_elements(ps.items) AS item
            WHERE (item->>'scheduleId')::uuid = :scheduleId
          )
        ORDER BY ps.created_at DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<PaymentSession> findCompletedSessionByUserAndSchedule(
        @Param("userId") UUID userId,
        @Param("scheduleId") UUID scheduleId
    );
}
