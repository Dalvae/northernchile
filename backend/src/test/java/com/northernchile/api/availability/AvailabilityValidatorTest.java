package com.northernchile.api.availability;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.cart.CartRepository;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailabilityValidator Tests - Anti-Overbooking System")
class AvailabilityValidatorTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private AvailabilityValidator availabilityValidator;

    private TourSchedule schedule;
    private UUID scheduleId;
    private static final int MAX_PARTICIPANTS = 10;

    @BeforeEach
    void setUp() {
        scheduleId = UUID.randomUUID();
        schedule = createMockSchedule(scheduleId, MAX_PARTICIPANTS);
    }

    @Test
    @DisplayName("Should show all slots available when no bookings or carts")
    void shouldShowAllSlotsAvailableWhenEmpty() {
        // Given
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(0);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(0);

        // When
        var result = availabilityValidator.validateAvailability(schedule, 5);

        // Then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getMaxParticipants()).isEqualTo(10);
        assertThat(result.getBookedParticipants()).isEqualTo(0);
        assertThat(result.getParticipantsInCarts()).isEqualTo(0);
        assertThat(result.getAvailableSlots()).isEqualTo(10);
        assertThat(result.getRequestedSlots()).isEqualTo(5);
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should calculate available slots with confirmed bookings only")
    void shouldCalculateAvailabilityWithBookingsOnly() {
        // Given
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(6);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(0);

        // When
        var result = availabilityValidator.validateAvailability(schedule, 3);

        // Then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getBookedParticipants()).isEqualTo(6);
        assertThat(result.getParticipantsInCarts()).isEqualTo(0);
        assertThat(result.getAvailableSlots()).isEqualTo(4); // 10 - 6 = 4
        assertThat(result.getRequestedSlots()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should calculate available slots with cart reservations only")
    void shouldCalculateAvailabilityWithCartsOnly() {
        // Given
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(0);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(4);

        // When
        var result = availabilityValidator.validateAvailability(schedule, 5);

        // Then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getBookedParticipants()).isEqualTo(0);
        assertThat(result.getParticipantsInCarts()).isEqualTo(4);
        assertThat(result.getAvailableSlots()).isEqualTo(6); // 10 - 4 = 6
    }

    @Test
    @DisplayName("Should calculate available slots with both bookings and carts")
    void shouldCalculateAvailabilityWithBookingsAndCarts() {
        // Given
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(5);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(3);

        // When
        var result = availabilityValidator.validateAvailability(schedule, 2);

        // Then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getBookedParticipants()).isEqualTo(5);
        assertThat(result.getParticipantsInCarts()).isEqualTo(3);
        assertThat(result.getAvailableSlots()).isEqualTo(2); // 10 - 5 - 3 = 2
        assertThat(result.getRequestedSlots()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should prevent overbooking when requested slots exceed available")
    void shouldPreventOverbookingWhenExceedingCapacity() {
        // Given
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(6);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(2);

        // When - Request 5 slots but only 2 available (10 - 6 - 2 = 2)
        var result = availabilityValidator.validateAvailability(schedule, 5);

        // Then
        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getAvailableSlots()).isEqualTo(2);
        assertThat(result.getRequestedSlots()).isEqualTo(5);
        assertThat(result.getErrorMessage()).isNotNull();
        assertThat(result.getErrorMessage()).contains("No hay suficientes cupos disponibles");
        assertThat(result.getErrorMessage()).contains("Solicitados: 5");
        assertThat(result.getErrorMessage()).contains("Disponibles: 2");
    }

    @Test
    @DisplayName("Should prevent overbooking when tour is sold out")
    void shouldPreventOverbookingWhenSoldOut() {
        // Given - All 10 slots taken
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(7);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(3);

        // When
        var result = availabilityValidator.validateAvailability(schedule, 1);

        // Then
        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getAvailableSlots()).isEqualTo(0);
        assertThat(result.getErrorMessage()).contains("Disponibles: 0");
    }

    @Test
    @DisplayName("Should allow booking last available slot")
    void shouldAllowBookingLastAvailableSlot() {
        // Given - Only 1 slot left
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(5);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(4);

        // When - Request exactly 1 slot
        var result = availabilityValidator.validateAvailability(schedule, 1);

        // Then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getAvailableSlots()).isEqualTo(1);
        assertThat(result.getRequestedSlots()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should exclude cart when validating cart update")
    void shouldExcludeCartWhenUpdating() {
        // Given
        UUID myCartId = UUID.randomUUID();
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(5);
        when(cartRepository.countParticipantsByScheduleIdExcludingCart(scheduleId, myCartId)).thenReturn(2);

        // When - Update cart (exclude my cart's 2 participants)
        var result = availabilityValidator.validateAvailability(schedule, 4, myCartId, null);

        // Then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getAvailableSlots()).isEqualTo(3); // 10 - 5 - 2 = 3
        assertThat(result.getRequestedSlots()).isEqualTo(4); // Still can't add 4
    }

    @Test
    @DisplayName("Should exclude user carts when validating for user")
    void shouldExcludeUserCartsWhenValidating() {
        // Given
        UUID userId = UUID.randomUUID();
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(4);
        when(cartRepository.countParticipantsByScheduleIdExcludingUser(scheduleId, userId)).thenReturn(3);

        // When
        var result = availabilityValidator.validateAvailability(schedule, 2, null, userId);

        // Then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getAvailableSlots()).isEqualTo(3); // 10 - 4 - 3 = 3
    }

    @Test
    @DisplayName("Should handle null counts from repositories")
    void shouldHandleNullCountsFromRepositories() {
        // Given - Repositories return null
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(null);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(null);

        // When
        var result = availabilityValidator.validateAvailability(schedule, 5);

        // Then - Should treat null as 0
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getBookedParticipants()).isEqualTo(0);
        assertThat(result.getParticipantsInCarts()).isEqualTo(0);
        assertThat(result.getAvailableSlots()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should get availability status without validating specific request")
    void shouldGetAvailabilityStatusWithoutRequest() {
        // Given
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(6);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(2);

        // When
        var status = availabilityValidator.getAvailabilityStatus(scheduleId, MAX_PARTICIPANTS);

        // Then
        assertThat(status.getMaxParticipants()).isEqualTo(10);
        assertThat(status.getBookedParticipants()).isEqualTo(6);
        assertThat(status.getParticipantsInCarts()).isEqualTo(2);
        assertThat(status.getAvailableSlots()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle zero requested slots")
    void shouldHandleZeroRequestedSlots() {
        // Given
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(5);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(3);

        // When
        var result = availabilityValidator.validateAvailability(schedule, 0);

        // Then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getRequestedSlots()).isEqualTo(0);
        assertThat(result.getAvailableSlots()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should calculate error message with correct counts")
    void shouldCalculateErrorMessageWithCorrectCounts() {
        // Given
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(6);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(3);

        // When
        var result = availabilityValidator.validateAvailability(schedule, 5);

        // Then
        assertThat(result.getErrorMessage()).isEqualTo(
                "No hay suficientes cupos disponibles. Solicitados: 5, Disponibles: 1 " +
                        "(Reservados: 6 confirmados + 3 en carritos)"
        );
    }

    @Test
    @DisplayName("Should return null error message when slots are available")
    void shouldReturnNullErrorMessageWhenAvailable() {
        // Given
        when(bookingRepository.countConfirmedParticipantsByScheduleId(scheduleId)).thenReturn(2);
        when(cartRepository.countParticipantsByScheduleId(scheduleId)).thenReturn(1);

        // When
        var result = availabilityValidator.validateAvailability(schedule, 5);

        // Then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
    }

    // Helper method to create mock TourSchedule
    private TourSchedule createMockSchedule(UUID id, int maxParticipants) {
        TourSchedule schedule = new TourSchedule();
        schedule.setId(id);
        schedule.setMaxParticipants(maxParticipants);
        schedule.setStartDatetime(Instant.now());

        Tour tour = new Tour();
        tour.setId(UUID.randomUUID());
        schedule.setTour(tour);

        return schedule;
    }
}
