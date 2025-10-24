package com.northernchile.api.availability;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    // TODO: Inject AvailabilityService

    @GetMapping
    public ResponseEntity<?> getAvailability(
            @RequestParam UUID tourId,
            @RequestParam String month) { // Format "YYYY-MM"

        // TODO: Implement logic to call the service
        // and return available days, unavailable days, windy days, full moon days, etc.

        YearMonth yearMonth = YearMonth.parse(month);
        String response = "Fetching availability for tour " + tourId + " for month " + yearMonth;
        return ResponseEntity.ok(response);
    }
}