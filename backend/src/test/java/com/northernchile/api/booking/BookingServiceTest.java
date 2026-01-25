package com.northernchile.api.booking;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.config.NotificationConfig;
import com.northernchile.api.exception.InvalidBookingStateException;
import com.northernchile.api.exception.ResourceNotFoundException;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.BookingStatus;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.TourScheduleStatus;
import com.northernchile.api.model.User;
import com.northernchile.api.notification.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BookingService Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private NotificationConfig notificationConfig;

    private BookingService bookingService;

    private User testUser;
    private TourSchedule testSchedule;
    private Tour testTour;

    @BeforeEach
    void setUp() {
        // Set up configuration mocks
        when(notificationConfig.getAdminEmail()).thenReturn("admin@northernchile.com");

        // Create service with all dependencies
        bookingService = new BookingService(
                bookingRepository,
                emailService,
                auditLogService,
                bookingMapper,
                notificationConfig
        );

        // Set up test user using constructor
        testUser = new User(
            UUID.randomUUID(),
            "test@example.com",
            null,
            "Test User",
            null,
            null,
            null,
            null,  // documentId
            "ROLE_CLIENT",
            "LOCAL",
            null
        );

        // Set up test tour
        testTour = new Tour();
        testTour.setId(UUID.randomUUID());
        testTour.setNameTranslations(Map.of("es", "Tour de Prueba", "en", "Test Tour"));
        testTour.setPrice(new BigDecimal("50000"));

        // Set up test schedule (future date)
        testSchedule = new TourSchedule();
        testSchedule.setId(UUID.randomUUID());
        testSchedule.setTour(testTour);
        testSchedule.setStartDatetime(Instant.now().plus(7, ChronoUnit.DAYS));
        testSchedule.setMaxParticipants(10);
        testSchedule.setStatus(TourScheduleStatus.OPEN);
    }

    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {

        private Booking existingBooking;

        @BeforeEach
        void setUp() {
            existingBooking = new Booking();
            existingBooking.setId(UUID.randomUUID());
            existingBooking.setUser(testUser);
            existingBooking.setSchedule(testSchedule);
            existingBooking.setStatus(BookingStatus.PENDING);
            existingBooking.setParticipants(new ArrayList<>());
        }

        @Test
        @DisplayName("Should allow PENDING -> CONFIRMED transition")
        void shouldAllowPendingToConfirmed() {
            // Given
            when(bookingRepository.findByIdWithDetails(existingBooking.getId()))
                    .thenReturn(Optional.of(existingBooking));
            when(bookingRepository.save(any())).thenReturn(existingBooking);
            when(bookingMapper.toBookingRes(any())).thenReturn(createMockBookingRes());

            // When
            BookingRes result = bookingService.updateBookingStatus(
                    existingBooking.getId(), BookingStatus.CONFIRMED, testUser);

            // Then
            assertThat(result).isNotNull();
            verify(bookingRepository).save(argThat(b -> BookingStatus.CONFIRMED.equals(b.getStatus())));
        }

        @Test
        @DisplayName("Should allow PENDING -> CANCELLED transition")
        void shouldAllowPendingToCancelled() {
            // Given
            when(bookingRepository.findByIdWithDetails(existingBooking.getId()))
                    .thenReturn(Optional.of(existingBooking));
            when(bookingRepository.save(any())).thenReturn(existingBooking);

            // When
            bookingService.cancelBooking(existingBooking.getId(), testUser);

            // Then
            verify(bookingRepository).save(argThat(b -> BookingStatus.CANCELLED.equals(b.getStatus())));
        }

        @Test
        @DisplayName("Should allow CONFIRMED -> COMPLETED transition")
        void shouldAllowConfirmedToCompleted() {
            // Given
            existingBooking.setStatus(BookingStatus.CONFIRMED);
            when(bookingRepository.findByIdWithDetails(existingBooking.getId()))
                    .thenReturn(Optional.of(existingBooking));
            when(bookingRepository.save(any())).thenReturn(existingBooking);
            when(bookingMapper.toBookingRes(any())).thenReturn(createMockBookingRes());

            // When
            BookingRes result = bookingService.updateBookingStatus(
                    existingBooking.getId(), BookingStatus.COMPLETED, testUser);

            // Then
            assertThat(result).isNotNull();
            verify(bookingRepository).save(argThat(b -> BookingStatus.COMPLETED.equals(b.getStatus())));
        }

        @Test
        @DisplayName("Should reject invalid transition from CANCELLED")
        void shouldRejectTransitionFromCancelled() {
            // Given
            existingBooking.setStatus(BookingStatus.CANCELLED);
            when(bookingRepository.findByIdWithDetails(existingBooking.getId()))
                    .thenReturn(Optional.of(existingBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(
                    existingBooking.getId(), BookingStatus.CONFIRMED, testUser))
                    .isInstanceOf(InvalidBookingStateException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("Should reject invalid transition from COMPLETED")
        void shouldRejectTransitionFromCompleted() {
            // Given
            existingBooking.setStatus(BookingStatus.COMPLETED);
            when(bookingRepository.findByIdWithDetails(existingBooking.getId()))
                    .thenReturn(Optional.of(existingBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(
                    existingBooking.getId(), BookingStatus.CANCELLED, testUser))
                    .isInstanceOf(InvalidBookingStateException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("Should reject PENDING -> COMPLETED (must go through CONFIRMED)")
        void shouldRejectPendingToCompleted() {
            // Given
            existingBooking.setStatus(BookingStatus.PENDING);
            when(bookingRepository.findByIdWithDetails(existingBooking.getId()))
                    .thenReturn(Optional.of(existingBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(
                    existingBooking.getId(), BookingStatus.COMPLETED, testUser))
                    .isInstanceOf(InvalidBookingStateException.class)
                    .hasMessageContaining("Invalid status transition");
        }
    }

    @Nested
    @DisplayName("Query Tests")
    class QueryTests {

        @Test
        @DisplayName("Should get bookings by user")
        void shouldGetBookingsByUser() {
            // Given
            Booking booking1 = new Booking();
            booking1.setId(UUID.randomUUID());
            Booking booking2 = new Booking();
            booking2.setId(UUID.randomUUID());

            when(bookingRepository.findByUserId(testUser.getId()))
                    .thenReturn(List.of(booking1, booking2));
            when(bookingMapper.toBookingRes(any())).thenReturn(createMockBookingRes());

            // When
            List<BookingRes> result = bookingService.getBookingsByUser(testUser);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should get booking by ID with authorization")
        void shouldGetBookingById() {
            // Given
            Booking booking = new Booking();
            booking.setId(UUID.randomUUID());
            booking.setUser(testUser);

            when(bookingRepository.findByIdWithDetails(booking.getId()))
                    .thenReturn(Optional.of(booking));
            when(bookingMapper.toBookingRes(booking)).thenReturn(createMockBookingRes());

            // When
            Optional<BookingRes> result = bookingService.getBookingById(booking.getId(), testUser);

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should throw exception when booking not found")
        void shouldThrowWhenBookingNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(bookingRepository.findByIdWithDetails(nonExistentId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingService.getBookingById(nonExistentId, testUser))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Admin Tests")
    class AdminTests {

        @Test
        @DisplayName("SUPER_ADMIN should get all bookings")
        void superAdminShouldGetAllBookings() {
            // Given
            User superAdmin = new User(
                UUID.randomUUID(),
                "admin@northernchile.com",
                null,
                "Super Admin",
                null,
                null,
                null,
                null,  // documentId
                "ROLE_SUPER_ADMIN",
                "LOCAL",
                null
            );

            Pageable pageable = PageRequest.of(0, 20);
            Page<Booking> bookingPage = new PageImpl<>(List.of(new Booking()));
            when(bookingRepository.findAllWithDetailsPaged(pageable)).thenReturn(bookingPage);
            when(bookingMapper.toBookingRes(any())).thenReturn(createMockBookingRes());

            // When
            Page<BookingRes> result = bookingService.getBookingsForAdminPaged(superAdmin, pageable);

            // Then
            verify(bookingRepository).findAllWithDetailsPaged(pageable);
            assertThat(result.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("PARTNER_ADMIN should only get their tour bookings")
        void partnerAdminShouldGetOwnTourBookings() {
            // Given
            User partnerAdmin = new User(
                UUID.randomUUID(),
                "partner@example.com",
                null,
                "Partner Admin",
                null,
                null,
                null,
                null,  // documentId
                "ROLE_PARTNER_ADMIN",
                "LOCAL",
                null
            );

            Pageable pageable = PageRequest.of(0, 20);
            Page<Booking> bookingPage = new PageImpl<>(List.of(new Booking()));
            when(bookingRepository.findByTourOwnerIdPaged(partnerAdmin.getId(), pageable))
                    .thenReturn(bookingPage);
            when(bookingMapper.toBookingRes(any())).thenReturn(createMockBookingRes());

            // When
            Page<BookingRes> result = bookingService.getBookingsForAdminPaged(partnerAdmin, pageable);

            // Then
            verify(bookingRepository).findByTourOwnerIdPaged(partnerAdmin.getId(), pageable);
            verify(bookingRepository, never()).findAllWithDetailsPaged(any());
        }
    }

    @Nested
    @DisplayName("Event Handler Tests")
    class EventHandlerTests {

        @Test
        @DisplayName("Should fetch booking when handling BookingCreatedEvent")
        void shouldFetchBookingOnBookingCreatedEvent() {
            // Given
            UUID bookingId = UUID.randomUUID();
            Booking booking = new Booking();
            booking.setId(bookingId);
            booking.setUser(testUser);
            booking.setSchedule(testSchedule);
            booking.setLanguageCode("es-CL");
            booking.setParticipants(new ArrayList<>());

            com.northernchile.api.booking.event.BookingCreatedEvent event =
                    new com.northernchile.api.booking.event.BookingCreatedEvent(bookingId);

            when(bookingRepository.findByIdWithDetails(bookingId))
                    .thenReturn(Optional.of(booking));

            // When
            bookingService.handleBookingCreated(event);

            // Then - verify booking was fetched for notification processing
            verify(bookingRepository).findByIdWithDetails(bookingId);
        }

        @Test
        @DisplayName("Should handle missing booking gracefully in event handler")
        void shouldHandleMissingBookingInEventHandler() {
            // Given
            UUID nonExistentBookingId = UUID.randomUUID();
            com.northernchile.api.booking.event.BookingCreatedEvent event =
                    new com.northernchile.api.booking.event.BookingCreatedEvent(nonExistentBookingId);

            when(bookingRepository.findByIdWithDetails(nonExistentBookingId))
                    .thenReturn(Optional.empty());

            // When - should not throw, just log error
            bookingService.handleBookingCreated(event);

            // Then - verify no crash and booking was attempted to be fetched
            verify(bookingRepository).findByIdWithDetails(nonExistentBookingId);
        }
    }

    // Helper method to create mock BookingRes for tests
    private BookingRes createMockBookingRes() {
        return new BookingRes(
            UUID.randomUUID(),       // id
            UUID.randomUUID(),       // userId
            "Test User",             // userFullName
            null,                    // userPhoneNumber
            UUID.randomUUID(),       // scheduleId
            "Test Tour",             // tourName
            java.time.LocalDate.now(), // tourDate
            java.time.LocalTime.of(10, 0), // tourStartTime
            "PENDING",               // status
            new BigDecimal("50000"), // subtotal
            new BigDecimal("9500"),  // taxAmount
            new BigDecimal("59500"), // totalAmount
            "es-CL",                 // languageCode
            null,                    // specialRequests
            Instant.now(),           // createdAt
            List.of()                // participants
        );
    }
}
