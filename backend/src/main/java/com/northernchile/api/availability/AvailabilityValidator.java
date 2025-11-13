package com.northernchile.api.availability;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.cart.CartRepository;
import com.northernchile.api.model.TourSchedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Centralized service for validating tour schedule availability and preventing overbooking.
 * This service ensures consistency across all booking and cart operations.
 */
@Service
public class AvailabilityValidator {

    private final BookingRepository bookingRepository;
    private final CartRepository cartRepository;

    public AvailabilityValidator(
            BookingRepository bookingRepository,
            CartRepository cartRepository) {
        this.bookingRepository = bookingRepository;
        this.cartRepository = cartRepository;
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

        // Calculate availability
        int totalReserved = bookedParticipants + participantsInCarts;
        int availableSlots = maxParticipants - totalReserved;
        boolean isAvailable = availableSlots >= requestedSlots;

        return new AvailabilityResult(
                isAvailable,
                maxParticipants,
                bookedParticipants,
                participantsInCarts,
                availableSlots,
                requestedSlots
        );
    }

    /**
     * Gets current availability status for a schedule without validating a specific request.
     *
     * @param scheduleId The tour schedule ID to check
     * @return AvailabilityResult with current status
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

        int totalReserved = bookedParticipants + participantsInCarts;
        int availableSlots = maxParticipants - totalReserved;

        return new AvailabilityStatus(
                maxParticipants,
                bookedParticipants,
                participantsInCarts,
                availableSlots
        );
    }

    /**
     * Result of availability validation.
     */
    public static class AvailabilityResult {
        private final boolean available;
        private final int maxParticipants;
        private final int bookedParticipants;
        private final int participantsInCarts;
        private final int availableSlots;
        private final int requestedSlots;

        public AvailabilityResult(
                boolean available,
                int maxParticipants,
                int bookedParticipants,
                int participantsInCarts,
                int availableSlots,
                int requestedSlots) {
            this.available = available;
            this.maxParticipants = maxParticipants;
            this.bookedParticipants = bookedParticipants;
            this.participantsInCarts = participantsInCarts;
            this.availableSlots = availableSlots;
            this.requestedSlots = requestedSlots;
        }

        public boolean isAvailable() {
            return available;
        }

        public int getMaxParticipants() {
            return maxParticipants;
        }

        public int getBookedParticipants() {
            return bookedParticipants;
        }

        public int getParticipantsInCarts() {
            return participantsInCarts;
        }

        public int getAvailableSlots() {
            return availableSlots;
        }

        public int getRequestedSlots() {
            return requestedSlots;
        }

        public String getErrorMessage() {
            if (available) {
                return null;
            }
            return String.format(
                    "No hay suficientes cupos disponibles. Solicitados: %d, Disponibles: %d " +
                            "(Reservados: %d confirmados + %d en carritos)",
                    requestedSlots, availableSlots, bookedParticipants, participantsInCarts
            );
        }
    }

    /**
     * Current availability status for a schedule.
     */
    public static class AvailabilityStatus {
        private final int maxParticipants;
        private final int bookedParticipants;
        private final int participantsInCarts;
        private final int availableSlots;

        public AvailabilityStatus(
                int maxParticipants,
                int bookedParticipants,
                int participantsInCarts,
                int availableSlots) {
            this.maxParticipants = maxParticipants;
            this.bookedParticipants = bookedParticipants;
            this.participantsInCarts = participantsInCarts;
            this.availableSlots = availableSlots;
        }

        public int getMaxParticipants() {
            return maxParticipants;
        }

        public int getBookedParticipants() {
            return bookedParticipants;
        }

        public int getParticipantsInCarts() {
            return participantsInCarts;
        }

        public int getAvailableSlots() {
            return availableSlots;
        }
    }
}
