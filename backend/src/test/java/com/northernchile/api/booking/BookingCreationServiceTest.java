package com.northernchile.api.booking;

import com.northernchile.api.availability.AvailabilityValidator;
import com.northernchile.api.booking.event.BookingEventPublisher;
import com.northernchile.api.exception.ScheduleFullException;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.payment.model.PaymentSession;
import com.northernchile.api.payment.model.PaymentSessionItem;
import com.northernchile.api.pricing.PricingService;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.user.SavedParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BookingCreationService Tests")
class BookingCreationServiceTest {

    @Mock
    private TourScheduleRepository scheduleRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AvailabilityValidator availabilityValidator;

    @Mock
    private PricingService pricingService;

    @Mock
    private SavedParticipantService savedParticipantService;

    @Mock
    private BookingEventPublisher eventPublisher;

    private BookingCreationServiceImpl bookingCreationService;

    private User testUser;
    private TourSchedule testSchedule;

    @BeforeEach
    void setUp() {
        bookingCreationService = new BookingCreationServiceImpl(
                scheduleRepository,
                bookingRepository,
                availabilityValidator,
                pricingService,
                savedParticipantService,
                eventPublisher
        );

        // Create test user
        testUser = new User(
                UUID.randomUUID(),
                "test@example.com",
                null,
                "Test User",
                null,
                null,
                null,
                null,
                "ROLE_CLIENT",
                "LOCAL",
                null
        );

        // Create test schedule
        testSchedule = new TourSchedule();
        testSchedule.setId(UUID.randomUUID());
        testSchedule.setMaxParticipants(10);
    }

    /**
     * Helper method to create a ParticipantData with all required fields
     */
    private PaymentSessionItem.ParticipantData createParticipantData(
            String fullName, boolean markAsSelf, boolean saveForFuture) {
        return new PaymentSessionItem.ParticipantData(
                fullName,
                "12345678-9",          // documentId
                "Chile",               // nationality
                LocalDate.of(1990, 1, 15), // dateOfBirth
                "Hotel Atacama",       // pickupAddress
                null,                  // specialRequirements
                "+56912345678",        // phoneNumber
                "john@example.com",    // email
                null,                  // savedParticipantId
                markAsSelf,            // markAsSelf
                saveForFuture          // saveForFuture
        );
    }

    /**
     * Helper method to create a PaymentSessionItem
     */
    private PaymentSessionItem createSessionItem(UUID scheduleId, int numParticipants) {
        List<PaymentSessionItem.ParticipantData> participants = new java.util.ArrayList<>();
        for (int i = 0; i < numParticipants; i++) {
            participants.add(createParticipantData("Participant " + i, false, false));
        }

        return new PaymentSessionItem(
                scheduleId,
                "Tour Test",
                LocalDate.now().plusDays(7),
                numParticipants,
                new BigDecimal("50000").multiply(BigDecimal.valueOf(numParticipants)),
                new BigDecimal("50000").multiply(BigDecimal.valueOf(numParticipants)),
                null,
                participants
        );
    }

    /**
     * Helper method to create a test PaymentSession
     */
    private PaymentSession createTestSession(List<PaymentSessionItem> items) {
        PaymentSession session = new PaymentSession();
        session.setId(UUID.randomUUID());
        session.setUser(testUser);
        session.setItems(items);
        session.setLanguageCode("es-CL");
        return session;
    }

    /**
     * Helper method to create a mock AvailabilityResult
     */
    private AvailabilityValidator.AvailabilityResult createAvailabilityResult(
            boolean available, int availableSlots) {
        return new AvailabilityValidator.AvailabilityResult(
                available,
                10,              // maxParticipants
                5,               // bookedParticipants
                2,               // participantsInCarts
                0,               // participantsInPaymentSessions
                availableSlots,  // availableSlots
                1                // requestedSlots
        );
    }

    @Nested
    @DisplayName("Happy Path Tests")
    class HappyPathTests {

        @Test
        @DisplayName("Should create booking successfully from payment session")
        void shouldCreateBookingFromPaymentSession() {
            // Given
            UUID scheduleId = testSchedule.getId();
            PaymentSessionItem item = createSessionItem(scheduleId, 1);
            PaymentSession session = createTestSession(List.of(item));

            when(scheduleRepository.findByIdWithLock(scheduleId))
                    .thenReturn(Optional.of(testSchedule));

            when(availabilityValidator.validateAvailability(any(), eq(1), any(), any()))
                    .thenReturn(createAvailabilityResult(true, 3));

            when(pricingService.calculateFromTaxInclusiveAmount(any()))
                    .thenReturn(new PricingService.PricingResult(
                            new BigDecimal("42016.81"),
                            new BigDecimal("7983.19"),
                            new BigDecimal("50000"),
                            new BigDecimal("0.19")
                    ));

            Booking savedBooking = new Booking();
            savedBooking.setId(UUID.randomUUID());
            when(bookingRepository.saveAndFlush(any())).thenReturn(savedBooking);

            // When
            List<UUID> bookingIds = bookingCreationService.createBookingsFromPaymentSession(session, testUser);

            // Then
            assertThat(bookingIds).hasSize(1);
            verify(bookingRepository).saveAndFlush(any(Booking.class));
            verify(eventPublisher).publishBookingCreated(savedBooking.getId());
        }

        @Test
        @DisplayName("Should create multiple bookings from session with multiple items")
        void shouldCreateMultipleBookingsFromSession() {
            // Given
            UUID scheduleId1 = UUID.randomUUID();
            UUID scheduleId2 = UUID.randomUUID();

            TourSchedule schedule1 = new TourSchedule();
            schedule1.setId(scheduleId1);
            schedule1.setMaxParticipants(10);

            TourSchedule schedule2 = new TourSchedule();
            schedule2.setId(scheduleId2);
            schedule2.setMaxParticipants(10);

            PaymentSessionItem item1 = createSessionItem(scheduleId1, 1);
            PaymentSessionItem item2 = createSessionItem(scheduleId2, 1);
            PaymentSession session = createTestSession(List.of(item1, item2));

            when(scheduleRepository.findByIdWithLock(scheduleId1)).thenReturn(Optional.of(schedule1));
            when(scheduleRepository.findByIdWithLock(scheduleId2)).thenReturn(Optional.of(schedule2));

            when(availabilityValidator.validateAvailability(any(), eq(1), any(), any()))
                    .thenReturn(createAvailabilityResult(true, 3));

            when(pricingService.calculateFromTaxInclusiveAmount(any()))
                    .thenReturn(new PricingService.PricingResult(
                            new BigDecimal("42016.81"),
                            new BigDecimal("7983.19"),
                            new BigDecimal("50000"),
                            new BigDecimal("0.19")
                    ));

            Booking savedBooking1 = new Booking();
            savedBooking1.setId(UUID.randomUUID());
            Booking savedBooking2 = new Booking();
            savedBooking2.setId(UUID.randomUUID());

            when(bookingRepository.saveAndFlush(any()))
                    .thenReturn(savedBooking1)
                    .thenReturn(savedBooking2);

            // When
            List<UUID> bookingIds = bookingCreationService.createBookingsFromPaymentSession(session, testUser);

            // Then
            assertThat(bookingIds).hasSize(2);
            verify(bookingRepository, times(2)).saveAndFlush(any(Booking.class));
            verify(eventPublisher, times(2)).publishBookingCreated(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("Overbooking Prevention Tests")
    class OverbookingPreventionTests {

        @Test
        @DisplayName("Should throw ScheduleFullException when no availability")
        void shouldThrowWhenScheduleFull() {
            // Given
            UUID scheduleId = testSchedule.getId();
            PaymentSessionItem item = createSessionItem(scheduleId, 5);
            PaymentSession session = createTestSession(List.of(item));

            when(scheduleRepository.findByIdWithLock(scheduleId))
                    .thenReturn(Optional.of(testSchedule));

            // Only 3 slots available, but 5 requested
            when(availabilityValidator.validateAvailability(any(), eq(5), any(), any()))
                    .thenReturn(createAvailabilityResult(false, 3));

            // When/Then
            assertThatThrownBy(() ->
                    bookingCreationService.createBookingsFromPaymentSession(session, testUser))
                    .isInstanceOf(ScheduleFullException.class)
                    .hasMessageContaining("No hay suficientes cupos disponibles");

            verify(bookingRepository, never()).saveAndFlush(any());
            verify(eventPublisher, never()).publishBookingCreated(any());
        }

        @Test
        @DisplayName("Should throw exception when schedule not found")
        void shouldThrowWhenScheduleNotFound() {
            // Given
            UUID nonExistentScheduleId = UUID.randomUUID();
            PaymentSessionItem item = createSessionItem(nonExistentScheduleId, 1);
            PaymentSession session = createTestSession(List.of(item));

            when(scheduleRepository.findByIdWithLock(nonExistentScheduleId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() ->
                    bookingCreationService.createBookingsFromPaymentSession(session, testUser))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Schedule not found");
        }
    }

    @Nested
    @DisplayName("Event Publishing Tests")
    class EventPublishingTests {

        @Test
        @DisplayName("Should publish BookingCreatedEvent after booking is saved")
        void shouldPublishEventAfterBookingSaved() {
            // Given
            UUID scheduleId = testSchedule.getId();
            UUID expectedBookingId = UUID.randomUUID();

            PaymentSessionItem item = createSessionItem(scheduleId, 1);
            PaymentSession session = createTestSession(List.of(item));

            when(scheduleRepository.findByIdWithLock(scheduleId))
                    .thenReturn(Optional.of(testSchedule));

            when(availabilityValidator.validateAvailability(any(), eq(1), any(), any()))
                    .thenReturn(createAvailabilityResult(true, 3));

            when(pricingService.calculateFromTaxInclusiveAmount(any()))
                    .thenReturn(new PricingService.PricingResult(
                            new BigDecimal("42016.81"),
                            new BigDecimal("7983.19"),
                            new BigDecimal("50000"),
                            new BigDecimal("0.19")
                    ));

            Booking savedBooking = new Booking();
            savedBooking.setId(expectedBookingId);
            when(bookingRepository.saveAndFlush(any())).thenReturn(savedBooking);

            // When
            bookingCreationService.createBookingsFromPaymentSession(session, testUser);

            // Then
            ArgumentCaptor<UUID> eventCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(eventPublisher).publishBookingCreated(eventCaptor.capture());
            assertThat(eventCaptor.getValue()).isEqualTo(expectedBookingId);
        }
    }
}
