package com.northernchile.api.availability;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<Map<LocalDate, AvailabilityService.DayAvailability>> getAvailability(
            @RequestParam UUID tourId,
            @RequestParam int year,
            @RequestParam int month) {
        Map<LocalDate, AvailabilityService.DayAvailability> availability =
                availabilityService.getAvailabilityForMonth(tourId, year, month);
        return new ResponseEntity<>(availability, HttpStatus.OK);
    }
}
