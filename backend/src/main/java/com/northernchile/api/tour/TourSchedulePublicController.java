package com.northernchile.api.tour;

import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.tour.dto.TourScheduleRes;
import com.northernchile.api.util.DateTimeUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(readOnly = true)
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

        // Filter by tourId and only include OPEN schedules
        // (ScheduleAutoCloseJob handles closing schedules within cutoff window)
        List<TourScheduleRes> response = schedules.stream()
                .filter(s -> s.getTour().getId().equals(tourId))
                .filter(s -> "OPEN".equals(s.getStatus()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private TourScheduleRes mapToResponse(TourSchedule schedule) {
        // Assigned guide info (optional)
        UUID assignedGuideId = null;
        String assignedGuideName = null;
        if (schedule.getAssignedGuide() != null) {
            assignedGuideId = schedule.getAssignedGuide().getId();
            assignedGuideName = schedule.getAssignedGuide().getFullName();
        }

        return new TourScheduleRes(
                schedule.getId(),
                schedule.getTour().getId(),
                schedule.getTour().getNameTranslations().get("es"), // Fallback
                schedule.getTour().getNameTranslations(),
                schedule.getTour().getDurationHours(),
                schedule.getStartDatetime(),
                schedule.getMaxParticipants(),
                0, // bookedParticipants - will be calculated elsewhere if needed
                schedule.getMaxParticipants(), // availableSpots - will be calculated elsewhere if needed
                schedule.getStatus(),
                assignedGuideId,
                assignedGuideName,
                schedule.getCreatedAt()
        );
    }
}
