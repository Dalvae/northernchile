package com.northernchile.api.reports;

import com.northernchile.api.reports.dto.BookingsByDayReport;
import com.northernchile.api.reports.dto.OverviewReport;
import com.northernchile.api.reports.dto.TopTourReport;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    /**
     * GET /api/admin/reports/overview
     * Business overview summary
     */
    @GetMapping("/overview")
    public ResponseEntity<OverviewReport> getOverview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Instant start = reportsService.parseStartDate(startDate);
        Instant end = reportsService.parseEndDate(endDate);

        OverviewReport report = reportsService.getOverview(start, end);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/admin/reports/bookings-by-day
     * Bookings grouped by day
     */
    @GetMapping("/bookings-by-day")
    public ResponseEntity<List<BookingsByDayReport>> getBookingsByDay(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Instant start = reportsService.parseStartDate(startDate);
        Instant end = reportsService.parseEndDate(endDate);

        List<BookingsByDayReport> report = reportsService.getBookingsByDay(start, end);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/admin/reports/top-tours
     * Most popular tours
     */
    @GetMapping("/top-tours")
    public ResponseEntity<List<TopTourReport>> getTopTours(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "10") int limit) {

        Instant start = reportsService.parseStartDate(startDate);
        Instant end = reportsService.parseEndDate(endDate);

        List<TopTourReport> report = reportsService.getTopTours(start, end, limit);
        return ResponseEntity.ok(report);
    }
}
