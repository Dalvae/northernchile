package com.northernchile.api.reports;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.Participant;
import com.northernchile.api.reports.dto.BookingsByDayReport;
import com.northernchile.api.reports.dto.OverviewReport;
import com.northernchile.api.reports.dto.TopTourReport;
import com.northernchile.api.tour.TourRepository;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportsService Tests")
class ReportsServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportsService reportsService;

    private Instant start;
    private Instant end;
    private List<Booking> mockBookings;

    @BeforeEach
    void setUp() {
        start = Instant.now().minus(30, ChronoUnit.DAYS);
        end = Instant.now();
        mockBookings = createMockBookings();
    }

    @Test
    @DisplayName("Should calculate overview report with correct metrics")
    void shouldCalculateOverviewWithCorrectMetrics() {
        // Given
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(mockBookings);
        when(userRepository.count()).thenReturn(100L);
        when(tourRepository.count()).thenReturn(20L);
        when(tourScheduleRepository.count()).thenReturn(150L);

        // When
        OverviewReport report = reportsService.getOverview(start, end);

        // Then
        assertThat(report).isNotNull();
        assertThat(report.getTotalBookings()).isEqualTo(4);
        assertThat(report.getConfirmedBookings()).isEqualTo(2);
        assertThat(report.getCancelledBookings()).isEqualTo(1);
        assertThat(report.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(report.getTotalParticipants()).isEqualTo(4);
        assertThat(report.getConversionRate()).isEqualTo(50.0); // 2/4 * 100
        assertThat(report.getTotalUsers()).isEqualTo(100L);
        assertThat(report.getTotalTours()).isEqualTo(20L);
        assertThat(report.getTotalSchedules()).isEqualTo(150L);
    }

    @Test
    @DisplayName("Should calculate average booking value correctly")
    void shouldCalculateAverageBookingValue() {
        // Given
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(mockBookings);
        when(userRepository.count()).thenReturn(100L);
        when(tourRepository.count()).thenReturn(20L);
        when(tourScheduleRepository.count()).thenReturn(150L);

        // When
        OverviewReport report = reportsService.getOverview(start, end);

        // Then
        // Total revenue = 300, Total bookings = 4, Average = 75
        assertThat(report.getAverageBookingValue()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    @DisplayName("Should handle empty bookings list")
    void shouldHandleEmptyBookingsList() {
        // Given
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(new ArrayList<>());
        when(userRepository.count()).thenReturn(100L);
        when(tourRepository.count()).thenReturn(20L);
        when(tourScheduleRepository.count()).thenReturn(150L);

        // When
        OverviewReport report = reportsService.getOverview(start, end);

        // Then
        assertThat(report.getTotalBookings()).isZero();
        assertThat(report.getConfirmedBookings()).isZero();
        assertThat(report.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(report.getAverageBookingValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(report.getConversionRate()).isZero();
    }

    @Test
    @DisplayName("Should group bookings by day correctly")
    void shouldGroupBookingsByDay() {
        // Given
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(mockBookings);

        // When
        List<BookingsByDayReport> report = reportsService.getBookingsByDay(start, end);

        // Then
        assertThat(report).isNotEmpty();
        assertThat(report).hasSizeGreaterThanOrEqualTo(1);

        // Verify reports are sorted by date
        for (int i = 0; i < report.size() - 1; i++) {
            assertThat(report.get(i).getDate())
                .isBeforeOrEqualTo(report.get(i + 1).getDate());
        }
    }

    @Test
    @DisplayName("Should aggregate revenue by day correctly")
    void shouldAggregateRevenueByDay() {
        // Given
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(mockBookings);

        // When
        List<BookingsByDayReport> report = reportsService.getBookingsByDay(start, end);

        // Then
        assertThat(report).isNotEmpty();
        report.forEach(dayReport -> {
            assertThat(dayReport.getDate()).isNotNull();
            assertThat(dayReport.getCount()).isPositive();
            assertThat(dayReport.getRevenue()).isNotNull();
        });
    }

    @Test
    @DisplayName("Should get top tours sorted by bookings count")
    void shouldGetTopToursSortedByBookingsCount() {
        // Given
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(mockBookings);

        // When
        List<TopTourReport> report = reportsService.getTopTours(start, end, 10);

        // Then
        assertThat(report).isNotEmpty();

        // Verify sorting (descending by bookings count)
        for (int i = 0; i < report.size() - 1; i++) {
            assertThat(report.get(i).getBookingsCount())
                .isGreaterThanOrEqualTo(report.get(i + 1).getBookingsCount());
        }
    }

    @Test
    @DisplayName("Should limit top tours to specified limit")
    void shouldLimitTopToursToSpecifiedLimit() {
        // Given
        List<Booking> manyBookings = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            manyBookings.addAll(mockBookings);
        }
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(manyBookings);

        // When
        List<TopTourReport> report = reportsService.getTopTours(start, end, 5);

        // Then
        assertThat(report).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should calculate participants count in top tours")
    void shouldCalculateParticipantsCountInTopTours() {
        // Given
        when(bookingRepository.findByCreatedAtBetween(any(), any())).thenReturn(mockBookings);

        // When
        List<TopTourReport> report = reportsService.getTopTours(start, end, 10);

        // Then
        assertThat(report).isNotEmpty();
        report.forEach(tourReport -> {
            assertThat(tourReport.getTourName()).isNotBlank();
            assertThat(tourReport.getBookingsCount()).isPositive();
            assertThat(tourReport.getRevenue()).isNotNull();
            assertThat(tourReport.getParticipants()).isNotNegative();
        });
    }

    @Test
    @DisplayName("Should parse start date correctly")
    void shouldParseStartDateCorrectly() {
        // When
        Instant parsed = reportsService.parseStartDate("2025-01-15");

        // Then
        assertThat(parsed).isNotNull();
        LocalDate date = LocalDate.ofInstant(parsed, ZoneId.of("America/Santiago"));
        assertThat(date).isEqualTo(LocalDate.of(2025, 1, 15));
    }

    @Test
    @DisplayName("Should parse end date correctly with day offset")
    void shouldParseEndDateCorrectlyWithDayOffset() {
        // When
        Instant parsed = reportsService.parseEndDate("2025-01-15");

        // Then
        assertThat(parsed).isNotNull();
        // End date should be start of next day (inclusive range)
        LocalDate date = LocalDate.ofInstant(parsed, ZoneId.of("America/Santiago"));
        assertThat(date).isEqualTo(LocalDate.of(2025, 1, 16));
    }

    @Test
    @DisplayName("Should use default dates when null provided")
    void shouldUseDefaultDatesWhenNullProvided() {
        // When
        Instant startParsed = reportsService.parseStartDate(null);
        Instant endParsed = reportsService.parseEndDate(null);

        // Then
        assertThat(startParsed).isNotNull();
        assertThat(endParsed).isNotNull();
        assertThat(startParsed).isBefore(endParsed);
    }

    // Helper method to create mock bookings
    private List<Booking> createMockBookings() {
        List<Booking> bookings = new ArrayList<>();

        // Confirmed booking 1
        Booking confirmed1 = createBooking("CONFIRMED", new BigDecimal("100.00"), 2);
        bookings.add(confirmed1);

        // Confirmed booking 2
        Booking confirmed2 = createBooking("CONFIRMED", new BigDecimal("200.00"), 2);
        bookings.add(confirmed2);

        // Pending booking
        Booking pending = createBooking("PENDING", new BigDecimal("150.00"), 1);
        bookings.add(pending);

        // Cancelled booking
        Booking cancelled = createBooking("CANCELLED", new BigDecimal("180.00"), 2);
        bookings.add(cancelled);

        return bookings;
    }

    private Booking createBooking(String status, BigDecimal totalAmount, int participantCount) {
        Booking booking = new Booking();
        booking.setStatus(status);
        booking.setTotalAmount(totalAmount);
        booking.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));

        // Create participants
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < participantCount; i++) {
            Participant p = new Participant();
            p.setFullName("Test Participant " + i);
            participants.add(p);
        }
        booking.setParticipants(participants);

        return booking;
    }
}
