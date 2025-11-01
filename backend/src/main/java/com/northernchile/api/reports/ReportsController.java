package com.northernchile.api.reports;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.tour.TourRepository;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
public class ReportsController {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final UserRepository userRepository;

    public ReportsController(
            BookingRepository bookingRepository,
            TourRepository tourRepository,
            TourScheduleRepository tourScheduleRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/admin/reports/overview
     * Resumen general del negocio
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        // Default: últimos 30 días
        Instant start = startDate != null
                ? LocalDate.parse(startDate).atStartOfDay(ZONE_ID).toInstant()
                : Instant.now().minus(30, ChronoUnit.DAYS);

        Instant end = endDate != null
                ? LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZONE_ID).toInstant()
                : Instant.now();

        // Obtener todas las reservas en el rango
        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(start, end);

        // Calcular métricas
        long totalBookings = bookings.size();
        long confirmedBookings = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                .count();
        long cancelledBookings = bookings.stream()
                .filter(b -> "CANCELLED".equals(b.getStatus()))
                .count();

        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalParticipants = bookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                .mapToLong(b -> b.getParticipants() != null ? b.getParticipants().size() : 0)
                .sum();

        // Métricas generales del sistema
        long totalUsers = userRepository.count();
        long totalTours = tourRepository.count();
        long totalSchedules = tourScheduleRepository.count();

        Map<String, Object> response = new HashMap<>();
        response.put("periodStart", start);
        response.put("periodEnd", end);
        response.put("totalBookings", totalBookings);
        response.put("confirmedBookings", confirmedBookings);
        response.put("cancelledBookings", cancelledBookings);
        response.put("totalRevenue", totalRevenue);
        response.put("totalParticipants", totalParticipants);
        response.put("averageBookingValue", totalBookings > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
        response.put("conversionRate", totalBookings > 0 ? (confirmedBookings * 100.0 / totalBookings) : 0);
        response.put("totalUsers", totalUsers);
        response.put("totalTours", totalTours);
        response.put("totalSchedules", totalSchedules);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/reports/bookings-by-day
     * Reservas agrupadas por día
     */
    @GetMapping("/bookings-by-day")
    public ResponseEntity<List<Map<String, Object>>> getBookingsByDay(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Instant start = startDate != null
                ? LocalDate.parse(startDate).atStartOfDay(ZONE_ID).toInstant()
                : Instant.now().minus(30, ChronoUnit.DAYS);

        Instant end = endDate != null
                ? LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZONE_ID).toInstant()
                : Instant.now();

        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(start, end);

        // Agrupar por día
        Map<LocalDate, List<Booking>> bookingsByDay = bookings.stream()
                .collect(Collectors.groupingBy(b ->
                        b.getCreatedAt().atZone(ZONE_ID).toLocalDate()
                ));

        List<Map<String, Object>> result = bookingsByDay.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", entry.getKey().toString());
                    dayData.put("count", entry.getValue().size());
                    dayData.put("revenue", entry.getValue().stream()
                            .filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                            .map(Booking::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return dayData;
                })
                .sorted((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/admin/reports/top-tours
     * Tours más populares
     */
    @GetMapping("/top-tours")
    public ResponseEntity<List<Map<String, Object>>> getTopTours(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "10") int limit) {

        Instant start = startDate != null
                ? LocalDate.parse(startDate).atStartOfDay(ZONE_ID).toInstant()
                : Instant.now().minus(30, ChronoUnit.DAYS);

        Instant end = endDate != null
                ? LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZONE_ID).toInstant()
                : Instant.now();

        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(start, end);

        // Agrupar por tour
        Map<String, List<Booking>> bookingsByTour = bookings.stream()
                .filter(b -> b.getSchedule() != null && b.getSchedule().getTour() != null)
                .collect(Collectors.groupingBy(b -> {
                    String tourName = b.getSchedule().getTour().getNameTranslations().get("es");
                    return tourName != null ? tourName : "Sin nombre";
                }));

        List<Map<String, Object>> result = bookingsByTour.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> tourData = new HashMap<>();
                    tourData.put("tourName", entry.getKey());
                    tourData.put("bookingsCount", entry.getValue().size());
                    tourData.put("revenue", entry.getValue().stream()
                            .filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                            .map(Booking::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    tourData.put("participants", entry.getValue().stream()
                            .filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                            .mapToLong(b -> b.getParticipants() != null ? b.getParticipants().size() : 0)
                            .sum());
                    return tourData;
                })
                .sorted((a, b) -> ((Integer) b.get("bookingsCount")).compareTo((Integer) a.get("bookingsCount")))
                .limit(limit)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
