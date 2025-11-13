package com.northernchile.api.reports;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.reports.dto.BookingsByDayReport;
import com.northernchile.api.reports.dto.OverviewReport;
import com.northernchile.api.reports.dto.TopTourReport;
import com.northernchile.api.tour.TourRepository;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportsService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");
    private static final int DEFAULT_PERIOD_DAYS = 30;

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final UserRepository userRepository;

    public ReportsService(
            BookingRepository bookingRepository,
            TourRepository tourRepository,
            TourScheduleRepository tourScheduleRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.userRepository = userRepository;
    }

    public OverviewReport getOverview(Instant start, Instant end) {
        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(start, end);

        long totalBookings = bookings.size();
        long confirmedBookings = countConfirmedBookings(bookings);
        long cancelledBookings = countCancelledBookings(bookings);
        BigDecimal totalRevenue = calculateTotalRevenue(bookings);
        long totalParticipants = countTotalParticipants(bookings);

        BigDecimal averageBookingValue = totalBookings > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        double conversionRate = totalBookings > 0
                ? (confirmedBookings * 100.0 / totalBookings)
                : 0.0;

        long totalUsers = userRepository.count();
        long totalTours = tourRepository.count();
        long totalSchedules = tourScheduleRepository.count();

        return new OverviewReport(
                start,
                end,
                totalBookings,
                confirmedBookings,
                cancelledBookings,
                totalRevenue,
                totalParticipants,
                averageBookingValue,
                conversionRate,
                totalUsers,
                totalTours,
                totalSchedules
        );
    }

    public List<BookingsByDayReport> getBookingsByDay(Instant start, Instant end) {
        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(start, end);

        Map<LocalDate, List<Booking>> bookingsByDay = bookings.stream()
                .collect(Collectors.groupingBy(b ->
                        b.getCreatedAt().atZone(ZONE_ID).toLocalDate()
                ));

        return bookingsByDay.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Booking> dayBookings = entry.getValue();
                    int count = dayBookings.size();
                    BigDecimal revenue = calculateTotalRevenue(dayBookings);
                    return new BookingsByDayReport(date, count, revenue);
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

    public List<TopTourReport> getTopTours(Instant start, Instant end, int limit) {
        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(start, end);

        Map<String, List<Booking>> bookingsByTour = bookings.stream()
                .filter(b -> b.getSchedule() != null && b.getSchedule().getTour() != null)
                .collect(Collectors.groupingBy(b -> {
                    String tourName = b.getSchedule().getTour().getNameTranslations().get("es");
                    return tourName != null ? tourName : "Sin nombre";
                }));

        return bookingsByTour.entrySet().stream()
                .map(entry -> {
                    String tourName = entry.getKey();
                    List<Booking> tourBookings = entry.getValue();
                    int bookingsCount = tourBookings.size();
                    BigDecimal revenue = calculateTotalRevenue(tourBookings);
                    long participants = countTotalParticipants(tourBookings);
                    return new TopTourReport(tourName, bookingsCount, revenue, participants);
                })
                .sorted((a, b) -> Integer.compare(b.getBookingsCount(), a.getBookingsCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Instant parseStartDate(String startDate) {
        return startDate != null
                ? LocalDate.parse(startDate).atStartOfDay(ZONE_ID).toInstant()
                : Instant.now().minus(DEFAULT_PERIOD_DAYS, ChronoUnit.DAYS);
    }

    public Instant parseEndDate(String endDate) {
        return endDate != null
                ? LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZONE_ID).toInstant()
                : Instant.now();
    }

    private long countConfirmedBookings(List<Booking> bookings) {
        return bookings.stream()
                .filter(this::isConfirmedOrCompleted)
                .count();
    }

    private long countCancelledBookings(List<Booking> bookings) {
        return bookings.stream()
                .filter(b -> "CANCELLED".equals(b.getStatus()))
                .count();
    }

    private BigDecimal calculateTotalRevenue(List<Booking> bookings) {
        return bookings.stream()
                .filter(this::isConfirmedOrCompleted)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countTotalParticipants(List<Booking> bookings) {
        return bookings.stream()
                .filter(this::isConfirmedOrCompleted)
                .mapToLong(b -> b.getParticipants() != null ? b.getParticipants().size() : 0)
                .sum();
    }

    private boolean isConfirmedOrCompleted(Booking booking) {
        String status = booking.getStatus();
        return "CONFIRMED".equals(status) || "COMPLETED".equals(status);
    }
}
