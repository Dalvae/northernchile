package com.northernchile.api.availability;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public ResponseEntity<Map<LocalDate, AvailabilityService.DayAvailability>> getAvailability(
            @RequestParam UUID tourId,
            @RequestParam String month) { // Format "YYYY-MM"

        YearMonth yearMonth = YearMonth.parse(month);
        Map<LocalDate, AvailabilityService.DayAvailability> availability =
                availabilityService.getAvailabilityForMonth(tourId, yearMonth.getYear(), yearMonth.getMonthValue());

        return ResponseEntity.ok(availability);
    }
}