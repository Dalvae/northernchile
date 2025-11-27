package com.northernchile.api.booking;

import com.northernchile.api.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduled job to clean up stale pending bookings.
 * Bookings that remain in PENDING status for too long (e.g., user abandoned checkout)
 * are cancelled to release the reserved slots.
 */
@Component
public class BookingCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(BookingCleanupJob.class);

    private final BookingRepository bookingRepository;

    @Value("${booking.cleanup.timeout-minutes:30}")
    private int timeoutMinutes;

    public BookingCleanupJob(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Runs every 5 minutes to cancel stale pending bookings.
     * A booking is considered stale if it has been in PENDING status for more than
     * the configured timeout (default 30 minutes).
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void cleanupStalePendingBookings() {
        Instant cutoff = Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES);
        log.debug("Running booking cleanup job. Cutoff time: {}", cutoff);

        List<Booking> staleBookings = bookingRepository.findStalePendingBookings(cutoff);

        if (staleBookings.isEmpty()) {
            log.debug("No stale pending bookings found");
            return;
        }

        log.info("Found {} stale pending bookings to cancel", staleBookings.size());

        for (Booking booking : staleBookings) {
            try {
                booking.setStatus("CANCELLED");
                bookingRepository.save(booking);
                log.info("Cancelled stale booking: {} (created at: {})",
                        booking.getId(), booking.getCreatedAt());
            } catch (Exception e) {
                log.error("Failed to cancel stale booking: {}", booking.getId(), e);
            }
        }

        log.info("Booking cleanup completed. Cancelled {} bookings", staleBookings.size());
    }
}
