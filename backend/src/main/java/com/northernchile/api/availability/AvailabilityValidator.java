package com.northernchile.api.availability;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.cart.CartRepository;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.payment.repository.PaymentSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Centralized service for validating tour schedule availability and preventing overbooking.
 * This service ensures consistency across all booking and cart operations.
 *
 * IMPORTANT: This validator counts slots from THREE sources:
 * 1. Confirmed bookings (CONFIRMED status)
 * 2. Items in shopping carts (soft reservation)
 * 3. Pending payment sessions (payment in progress, not yet confirmed)
 *
 * This prevents overbooking when multiple users are attempting to book simultaneously.
 */
@Service
public class AvailabilityValidator {

    private final BookingRepository bookingRepository;
    private final CartRepository cartRepository;
    private final PaymentSessionRepository paymentSessionRepository;

    public AvailabilityValidator(
            BookingRepository bookingRepository,
            CartRepository cartRepository,
            PaymentSessionRepository paymentSessionRepository) {
        this.bookingRepository = bookingRepository;
        this.cartRepository = cartRepository;
        this.paymentSessionRepository = paymentSessionRepository;
    }

    /**
     * Validates if the requested number of slots is available for a schedule.
     * Takes into account both confirmed bookings and items in carts.
     *
     * @param schedule The tour schedule to check
     * @param requestedSlots Number of slots being requested
     * @return AvailabilityResult with validation details
     */
    @Transactional(readOnly = true)
    public AvailabilityResult validateAvailability(TourSchedule schedule, int requestedSlots) {
        return validateAvailability(schedule, requestedSlots, null, null);
    }

    /**
     * Validates availability excluding slots from a specific cart (for cart updates).
     *
     * @param schedule The tour schedule to check
     * @param requestedSlots Number of slots being requested
     * @param excludeCartId Cart ID to exclude from count (null to include all)
     * @param excludeUserId User ID to exclude cart items from (null to include all)
     * @return AvailabilityResult with validation details
     */
    @Transactional(readOnly = true)
    public AvailabilityResult validateAvailability(
            TourSchedule schedule,
            int requestedSlots,
            UUID excludeCartId,
            UUID excludeUserId) {

        UUID scheduleId = schedule.getId();
        int maxParticipants = schedule.getMaxParticipants();

        // Count confirmed participants from bookings
        Integer bookedParticipants = bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId);
        if (bookedParticipants == null) {
            bookedParticipants = 0;
        }

        // Count participants in carts (reserved but not yet confirmed)
        Integer participantsInCarts;
        if (excludeCartId != null) {
            participantsInCarts = cartRepository.countParticipantsByScheduleIdExcludingCart(scheduleId, excludeCartId);
        } else if (excludeUserId != null) {
            participantsInCarts = cartRepository.countParticipantsByScheduleIdExcludingUser(scheduleId, excludeUserId);
        } else {
            participantsInCarts = cartRepository.countParticipantsByScheduleId(scheduleId);
        }
        if (participantsInCarts == null) {
            participantsInCarts = 0;
        }

        // Count participants in pending payment sessions (payment in progress)
        // These are slots reserved while payment is being processed
        int participantsInPaymentSessions = paymentSessionRepository
            .countReservedSlotsByScheduleId(scheduleId, Instant.now());

        // Calculate availability including all reservation sources
        int totalReserved = bookedParticipants + participantsInCarts + participantsInPaymentSessions;
        int availableSlots = maxParticipants - totalReserved;
        boolean isAvailable = availableSlots >= requestedSlots;

        return new AvailabilityResult(
                isAvailable,
                maxParticipants,
                bookedParticipants,
                participantsInCarts,
                participantsInPaymentSessions,
                availableSlots,
                requestedSlots
        );
    }

    /**
     * Gets current availability status for a schedule without validating a specific request.
     *
     * @param scheduleId The tour schedule ID to check
     * @return AvailabilityStatus with current status
     */
    @Transactional(readOnly = true)
    public AvailabilityStatus getAvailabilityStatus(UUID scheduleId, int maxParticipants) {
        Integer bookedParticipants = bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId);
        if (bookedParticipants == null) {
            bookedParticipants = 0;
        }

        Integer participantsInCarts = cartRepository.countParticipantsByScheduleId(scheduleId);
        if (participantsInCarts == null) {
            participantsInCarts = 0;
        }

        int participantsInPaymentSessions = paymentSessionRepository
            .countReservedSlotsByScheduleId(scheduleId, Instant.now());

        int totalReserved = bookedParticipants + participantsInCarts + participantsInPaymentSessions;
        int availableSlots = maxParticipants - totalReserved;

        return new AvailabilityStatus(
                maxParticipants,
                bookedParticipants,
                participantsInCarts,
                participantsInPaymentSessions,
                availableSlots
        );
    }

    /**
     * Result of availability validation.
     */
    public record AvailabilityResult(
            boolean available,
            int maxParticipants,
            int bookedParticipants,
            int participantsInCarts,
            int participantsInPaymentSessions,
            int availableSlots,
            int requestedSlots
    ) {
        /**
         * Returns error message if not available, null otherwise.
         */
        public String errorMessage() {
            if (available) {
                return null;
            }
            return String.format(
                    "No hay suficientes cupos disponibles. Solicitados: %d, Disponibles: %d " +
                            "(Reservados: %d confirmados + %d en carritos + %d en proceso de pago)",
                    requestedSlots, availableSlots, bookedParticipants, participantsInCarts, participantsInPaymentSessions
            );
        }
    }

    /**
     * Current availability status for a schedule.
     */
    public record AvailabilityStatus(
            int maxParticipants,
            int bookedParticipants,
            int participantsInCarts,
            int participantsInPaymentSessions,
            int availableSlots
    ) {}
}
