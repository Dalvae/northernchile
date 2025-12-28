package com.northernchile.api.payment;

import com.northernchile.api.payment.repository.PaymentSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled job to expire old pending payment sessions.
 * Sessions that remain in PENDING status past their expiration time are marked as EXPIRED.
 */
@Component
public class PaymentSessionCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentSessionCleanupJob.class);

    private final PaymentSessionService sessionService;

    public PaymentSessionCleanupJob(PaymentSessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Runs every 15 minutes to expire old pending payment sessions.
     * Interval increased from 5 min to reduce database compute costs on Neon.
     */
    @Scheduled(fixedRate = 900000) // Every 15 minutes
    @Transactional
    public void cleanupExpiredSessions() {
        log.debug("Running payment session cleanup job");

        int expiredCount = sessionService.expirePendingSessions();

        if (expiredCount > 0) {
            log.info("Payment session cleanup completed. Expired {} sessions", expiredCount);
        }
    }
}
