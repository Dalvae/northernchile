package com.northernchile.api.booking;

import com.northernchile.api.model.User;
import com.northernchile.api.payment.model.PaymentSession;

import java.util.List;
import java.util.UUID;

/**
 * Service for creating bookings from payment sessions.
 * Extracted from PaymentSessionService to improve separation of concerns.
 *
 * <p>This service handles the creation of Booking entities after a payment has been
 * successfully processed. It validates availability, calculates pricing, creates
 * participants, and triggers confirmation notifications.</p>
 */
public interface BookingCreationService {

    /**
     * Creates bookings from a completed payment session.
     *
     * <p>This method performs the following operations for each item in the session:
     * <ul>
     *   <li>Acquires a pessimistic lock on the schedule to prevent race conditions</li>
     *   <li>Re-validates availability with the lock held</li>
     *   <li>Calculates pricing with proper tax breakdown</li>
     *   <li>Creates the booking with CONFIRMED status (already paid)</li>
     *   <li>Creates participant records from session data</li>
     *   <li>Optionally saves participant data to user profile</li>
     *   <li>Sends booking confirmation notifications</li>
     * </ul>
     *
     * @param session The payment session containing items and participant data
     * @param user The user who owns the bookings
     * @return List of created booking IDs
     * @throws com.northernchile.api.exception.ScheduleFullException if availability check fails
     * @throws IllegalStateException if a schedule is not found
     */
    List<UUID> createBookingsFromPaymentSession(PaymentSession session, User user);
}
