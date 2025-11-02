package com.northernchile.api.tour;

import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.tour.dto.TourScheduleRes;
import com.northernchile.api.util.DateTimeUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tours")
public class TourSchedulePublicController {

    private final TourScheduleRepository tourScheduleRepository;

    public TourSchedulePublicController(TourScheduleRepository tourScheduleRepository) {
        this.tourScheduleRepository = tourScheduleRepository;
    }

    /**
     * GET /api/tours/{tourId}/schedules?start=2025-11-01&end=2025-12-31
     * Public endpoint para obtener schedules de un tour específico
     */
    @GetMapping("/{tourId}/schedules")
    public ResponseEntity<List<TourScheduleRes>> getTourSchedules(
            @PathVariable UUID tourId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        Instant startInstant = DateTimeUtils.toInstantStartOfDay(start);
        Instant endInstant = DateTimeUtils.toInstantEndOfDay(end);

        // Use JOIN FETCH to avoid LazyInitializationException
        List<TourSchedule> schedules = tourScheduleRepository.findByStartDatetimeBetweenWithTour(
                startInstant,
                endInstant
        );

        // Filter by tourId and only include OPEN schedules for public
        List<TourScheduleRes> response = schedules.stream()
                .filter(s -> s.getTour().getId().equals(tourId))
                .filter(s -> "OPEN".equals(s.getStatus()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private TourScheduleRes mapToResponse(TourSchedule schedule) {
        TourScheduleRes res = new TourScheduleRes();
        res.setId(schedule.getId());
        res.setTourId(schedule.getTour().getId());
        res.setStartDatetime(schedule.getStartDatetime());
        res.setMaxParticipants(schedule.getMaxParticipants());
        res.setStatus(schedule.getStatus());
        res.setCreatedAt(schedule.getCreatedAt());

        // Add tour info for frontend
        res.setTourName(schedule.getTour().getNameTranslations().get("es")); // Fallback
        res.setTourNameTranslations(schedule.getTour().getNameTranslations());
        res.setTourDurationHours(schedule.getTour().getDurationHours());

        // Assigned guide info (optional)
        if (schedule.getAssignedGuide() != null) {
            res.setAssignedGuideId(schedule.getAssignedGuide().getId());
            res.setAssignedGuideName(schedule.getAssignedGuide().getFullName());
        }

        return res;
    }
}
