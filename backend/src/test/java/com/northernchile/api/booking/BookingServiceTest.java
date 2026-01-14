package com.northernchile.api.booking;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.availability.AvailabilityValidator;
import com.northernchile.api.availability.AvailabilityValidator.AvailabilityResult;
import com.northernchile.api.booking.dto.BookingCreateReq;
import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.booking.dto.ParticipantReq;
import com.northernchile.api.config.NotificationConfig;
import com.northernchile.api.config.properties.AppProperties;
import com.northernchile.api.exception.BookingCutoffException;
import com.northernchile.api.exception.InvalidBookingStateException;
import com.northernchile.api.exception.ResourceNotFoundException;
import com.northernchile.api.exception.ScheduleFullException;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.Participant;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.notification.EmailService;
import com.northernchile.api.pricing.PricingService;
import com.northernchile.api.tour.TourScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BookingService Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private AvailabilityValidator availabilityValidator;

    @Mock
    private PricingService pricingService;

    @Mock
    private NotificationConfig notificationConfig;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Booking bookingProperties;

    private BookingService bookingService;

    private User testUser;
    private TourSchedule testSchedule;
    private Tour testTour;
    private BookingCreateReq validBookingRequest;

    @BeforeEach
    void setUp() {
        // Set up configuration mocks
        when(notificationConfig.getAdminEmail()).thenReturn("admin@northernchile.com");
        when(appProperties.getBooking()).thenReturn(bookingProperties);
        when(bookingProperties.getMinHoursBeforeTour()).thenReturn(2);

        // Create service with all dependencies
        bookingService = new BookingService(
                bookingRepository,
                tourScheduleRepository,
                emailService,
                auditLogService,
                bookingMapper,
                availabilityValidator,
                pricingService,
                notificationConfig,
                appProperties
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
        testSchedule.setStatus("OPEN");

        // Set up valid booking request using record constructors
        ParticipantReq participant = new ParticipantReq(
            "Test Participant",  // fullName
            "12345678-9",        // documentId
            "CL",                // nationality
            null,                // dateOfBirth
            null,                // age
            null,                // pickupAddress
            null,                // specialRequirements
            null,                // phoneNumber
            "participant@example.com"  // email
        );
        validBookingRequest = new BookingCreateReq(
            testSchedule.getId(),
            List.of(participant),
            "es-CL",
            null  // specialRequests
        );

        // Set up pricing service mock
        PricingService.PricingResult pricingResult = new PricingService.PricingResult(
            new BigDecimal("50000"),   // subtotal
            new BigDecimal("9500"),    // taxAmount
            new BigDecimal("59500"),   // totalAmount
            new BigDecimal("0.19")     // taxRate
        );
        when(pricingService.calculateLineItem(any(BigDecimal.class), anyInt())).thenReturn(pricingResult);
    }

    @Nested
    @DisplayName("Create Booking Tests")
    class CreateBookingTests {

        @Test
        @DisplayName("Should create booking successfully with valid data")
        void shouldCreateBookingSuccessfully() {
            // Given
            when(tourScheduleRepository.findByIdWithLock(testSchedule.getId()))
                    .thenReturn(Optional.of(testSchedule));
            when(availabilityValidator.validateAvailability(any(), anyInt(), any(), any()))
                    .thenReturn(new AvailabilityResult(true, 10, 1, 0, 9, 1));
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(invocation -> {
                        Booking booking = invocation.getArgument(0);
                        booking.setId(UUID.randomUUID());
                        return booking;
                    });
            when(bookingMapper.toBookingRes(any(Booking.class)))
                    .thenReturn(createMockBookingRes());

            // When
            BookingRes result = bookingService.createBooking(validBookingRequest, testUser);

            // Then
            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
            // Note: Email is sent after payment confirmation, not on booking creation
            verify(emailService, never()).sendBookingConfirmationEmail(
                    any(), any(), any(), any(), any(), any(), anyInt(), any(), any()
            );
        }

        @Test
        @DisplayName("Should reject booking when schedule not found")
        void shouldRejectBookingWhenScheduleNotFound() {
            // Given
            when(tourScheduleRepository.findByIdWithLock(any()))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingService.createBooking(validBookingRequest, testUser))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("TourSchedule not found");
        }

        @Test
        @DisplayName("Should reject booking when no availability")
        void shouldRejectBookingWhenNoAvailability() {
            // Given
            when(tourScheduleRepository.findByIdWithLock(testSchedule.getId()))
                    .thenReturn(Optional.of(testSchedule));
            when(availabilityValidator.validateAvailability(any(), anyInt(), any(), any()))
                    .thenReturn(new AvailabilityResult(false, 10, 10, 0, 0, 1));

            // When/Then
            assertThatThrownBy(() -> bookingService.createBooking(validBookingRequest, testUser))
                    .isInstanceOf(ScheduleFullException.class)
                    .hasMessageContaining("No hay suficientes cupos disponibles");
        }

        @Test
        @DisplayName("Should reject booking when too close to tour start")
        void shouldRejectBookingWhenTooCloseToTourStart() {
            // Given - Schedule starts in 1 hour (less than minHoursBeforeTour=2)
            testSchedule.setStartDatetime(Instant.now().plus(1, ChronoUnit.HOURS));
            when(tourScheduleRepository.findByIdWithLock(testSchedule.getId()))
                    .thenReturn(Optional.of(testSchedule));

            // When/Then
            assertThatThrownBy(() -> bookingService.createBooking(validBookingRequest, testUser))
                    .isInstanceOf(BookingCutoffException.class)
                    .hasMessageContaining("Bookings for this tour are closed");
        }

        @Test
        @DisplayName("Should calculate total amount correctly with tax")
        void shouldCalculateTotalAmountCorrectly() {
            // Given
            when(tourScheduleRepository.findByIdWithLock(testSchedule.getId()))
                    .thenReturn(Optional.of(testSchedule));
            when(availabilityValidator.validateAvailability(any(), anyInt(), any(), any()))
                    .thenReturn(new AvailabilityResult(true, 10, 1, 0, 9, 1));

            ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
            when(bookingRepository.save(bookingCaptor.capture()))
                    .thenAnswer(invocation -> {
                        Booking booking = invocation.getArgument(0);
                        booking.setId(UUID.randomUUID());
                        return booking;
                    });
            when(bookingMapper.toBookingRes(any())).thenReturn(createMockBookingRes());

            // When
            bookingService.createBooking(validBookingRequest, testUser);

            // Then
            Booking savedBooking = bookingCaptor.getValue();
            // Now uses PricingService which returns totalAmount (59500) = subtotal (50000) + tax (9500)
            assertThat(savedBooking.getTotalAmount()).isEqualByComparingTo(new BigDecimal("59500"));
            assertThat(savedBooking.getSubtotal()).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(savedBooking.getTaxAmount()).isEqualByComparingTo(new BigDecimal("9500"));
            assertThat(savedBooking.getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("Should create booking with multiple participants")
        void shouldCreateBookingWithMultipleParticipants() {
            // Given
            ParticipantReq p1 = new ParticipantReq(
                "Participant 1", "11111111-1", "CL", null, null, null, null, null, "p1@example.com"
            );
            ParticipantReq p2 = new ParticipantReq(
                "Participant 2", "22222222-2", "AR", null, null, null, null, null, "p2@example.com"
            );

            // Create a new booking request with multiple participants
            validBookingRequest = new BookingCreateReq(
                testSchedule.getId(),
                List.of(p1, p2),
                "es-CL",
                null
            );

            // Set up pricing for 2 participants
            PricingService.PricingResult pricingResult2 = new PricingService.PricingResult(
                new BigDecimal("100000"),  // subtotal = 50000 * 2
                new BigDecimal("19000"),   // taxAmount
                new BigDecimal("119000"),  // totalAmount
                new BigDecimal("0.19")     // taxRate
            );
            when(pricingService.calculateLineItem(any(BigDecimal.class), eq(2))).thenReturn(pricingResult2);

            when(tourScheduleRepository.findByIdWithLock(testSchedule.getId()))
                    .thenReturn(Optional.of(testSchedule));
            when(availabilityValidator.validateAvailability(any(), eq(2), any(), any()))
                    .thenReturn(new AvailabilityResult(true, 10, 2, 0, 8, 2));

            ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
            when(bookingRepository.save(bookingCaptor.capture()))
                    .thenAnswer(invocation -> {
                        Booking booking = invocation.getArgument(0);
                        booking.setId(UUID.randomUUID());
                        return booking;
                    });
            when(bookingMapper.toBookingRes(any())).thenReturn(createMockBookingRes());

            // When
            bookingService.createBooking(validBookingRequest, testUser);

            // Then
            Booking savedBooking = bookingCaptor.getValue();
            assertThat(savedBooking.getParticipants()).hasSize(2);
            // Total = 50000 * 2 + tax = 119000
            assertThat(savedBooking.getTotalAmount()).isEqualByComparingTo(new BigDecimal("119000"));
            assertThat(savedBooking.getSubtotal()).isEqualByComparingTo(new BigDecimal("100000"));
        }
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
            existingBooking.setStatus("PENDING");
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
                    existingBooking.getId(), "CONFIRMED", testUser);

            // Then
            assertThat(result).isNotNull();
            verify(bookingRepository).save(argThat(b -> "CONFIRMED".equals(b.getStatus())));
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
            verify(bookingRepository).save(argThat(b -> "CANCELLED".equals(b.getStatus())));
        }

        @Test
        @DisplayName("Should allow CONFIRMED -> COMPLETED transition")
        void shouldAllowConfirmedToCompleted() {
            // Given
            existingBooking.setStatus("CONFIRMED");
            when(bookingRepository.findByIdWithDetails(existingBooking.getId()))
                    .thenReturn(Optional.of(existingBooking));
            when(bookingRepository.save(any())).thenReturn(existingBooking);
            when(bookingMapper.toBookingRes(any())).thenReturn(createMockBookingRes());

            // When
            BookingRes result = bookingService.updateBookingStatus(
                    existingBooking.getId(), "COMPLETED", testUser);

            // Then
            assertThat(result).isNotNull();
            verify(bookingRepository).save(argThat(b -> "COMPLETED".equals(b.getStatus())));
        }

        @Test
        @DisplayName("Should reject invalid transition from CANCELLED")
        void shouldRejectTransitionFromCancelled() {
            // Given
            existingBooking.setStatus("CANCELLED");
            when(bookingRepository.findByIdWithDetails(existingBooking.getId()))
                    .thenReturn(Optional.of(existingBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(
                    existingBooking.getId(), "CONFIRMED", testUser))
                    .isInstanceOf(InvalidBookingStateException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("Should reject invalid transition from COMPLETED")
        void shouldRejectTransitionFromCompleted() {
            // Given
            existingBooking.setStatus("COMPLETED");
            when(bookingRepository.findByIdWithDetails(existingBooking.getId()))
                    .thenReturn(Optional.of(existingBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(
                    existingBooking.getId(), "CANCELLED", testUser))
                    .isInstanceOf(InvalidBookingStateException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("Should reject PENDING -> COMPLETED (must go through CONFIRMED)")
        void shouldRejectPendingToCompleted() {
            // Given
            existingBooking.setStatus("PENDING");
            when(bookingRepository.findByIdWithDetails(existingBooking.getId()))
                    .thenReturn(Optional.of(existingBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(
                    existingBooking.getId(), "COMPLETED", testUser))
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
    @DisplayName("Mock Payment Confirmation Tests")
    class MockPaymentTests {

        @Test
        @DisplayName("Should confirm booking after mock payment")
        void shouldConfirmBookingAfterMockPayment() {
            // Given
            Booking pendingBooking = new Booking();
            pendingBooking.setId(UUID.randomUUID());
            pendingBooking.setUser(testUser);
            pendingBooking.setSchedule(testSchedule);
            pendingBooking.setStatus("PENDING");

            when(bookingRepository.findByIdWithDetails(pendingBooking.getId()))
                    .thenReturn(Optional.of(pendingBooking));
            when(bookingRepository.save(any())).thenReturn(pendingBooking);
            when(bookingMapper.toBookingRes(any())).thenReturn(createMockBookingRes());

            // When
            BookingRes result = bookingService.confirmBookingAfterMockPayment(
                    pendingBooking.getId(), testUser);

            // Then
            assertThat(result).isNotNull();
            verify(bookingRepository).save(argThat(b -> "CONFIRMED".equals(b.getStatus())));
        }

        @Test
        @DisplayName("Should reject mock confirmation for non-PENDING booking")
        void shouldRejectMockConfirmationForNonPending() {
            // Given
            Booking confirmedBooking = new Booking();
            confirmedBooking.setId(UUID.randomUUID());
            confirmedBooking.setStatus("CONFIRMED");

            when(bookingRepository.findByIdWithDetails(confirmedBooking.getId()))
                    .thenReturn(Optional.of(confirmedBooking));

            // When/Then
            assertThatThrownBy(() -> bookingService.confirmBookingAfterMockPayment(
                    confirmedBooking.getId(), testUser))
                    .isInstanceOf(InvalidBookingStateException.class)
                    .hasMessageContaining("Only PENDING bookings");
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
