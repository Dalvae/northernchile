package com.northernchile.api.tour.schedule;

import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.tour.TourScheduleGeneratorService;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.tour.dto.TourScheduleCreateReq;
import com.northernchile.api.tour.dto.TourScheduleRes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/schedules")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
public class TourScheduleAdminController {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");

    private final TourScheduleRepository tourScheduleRepository;
    private final TourScheduleService tourScheduleService;
    private final TourScheduleGeneratorService generatorService;

    public TourScheduleAdminController(
            TourScheduleRepository tourScheduleRepository,
            TourScheduleService tourScheduleService,
            TourScheduleGeneratorService generatorService) {
        this.tourScheduleRepository = tourScheduleRepository;
        this.tourScheduleService = tourScheduleService;
        this.generatorService = generatorService;
    }

    /**
     * GET /api/admin/schedules?start=2025-11-01&end=2025-11-14
     * Obtiene todos los schedules en un rango de fechas (para el calendario)
     */
    @GetMapping
    public ResponseEntity<List<TourScheduleRes>> getSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        ZonedDateTime startDateTime = start.atStartOfDay(ZONE_ID);
        ZonedDateTime endDateTime = end.plusDays(1).atStartOfDay(ZONE_ID);

        Instant startInstant = startDateTime.toInstant();
        Instant endInstant = endDateTime.toInstant();

        List<TourSchedule> schedules = tourScheduleRepository.findByTourIdAndStartDatetimeBetween(
                null, // TODO: Filter by owner for PARTNER_ADMIN
                startInstant,
                endInstant
        );

        List<TourScheduleRes> response = schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/schedules
     * Crea un schedule manualmente (para David y casos especiales)
     */
    @PostMapping
    public ResponseEntity<TourScheduleRes> createSchedule(@RequestBody TourScheduleCreateReq request) {
        TourSchedule created = tourScheduleService.createSchedule(request);
        return ResponseEntity.ok(mapToResponse(created));
    }

    /**
     * PATCH /api/admin/schedules/{id}
     * Edita un schedule existente (cambiar hora, cupos, etc.)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<TourScheduleRes> updateSchedule(
            @PathVariable String id,
            @RequestBody TourScheduleCreateReq request) {
        // TODO: Implement update logic
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/admin/schedules/{id}
     * Cancela un schedule (cambia status a CANCELLED)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelSchedule(@PathVariable String id) {
        // TODO: Implement cancel logic (set status to CANCELLED)
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/admin/schedules/generate
     * Trigger manual del generador automático (útil para testing)
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateSchedules() {
        generatorService.generateSchedulesManually();
        return ResponseEntity.ok("Schedule generation triggered successfully");
    }

    private TourScheduleRes mapToResponse(TourSchedule schedule) {
        TourScheduleRes res = new TourScheduleRes();
        res.setId(schedule.getId());
        res.setStartDatetime(schedule.getStartDatetime());
        res.setMaxParticipants(schedule.getMaxParticipants());
        res.setStatus(schedule.getStatus());

        // TODO: Map tour info, assigned guide, booking count, etc.

        return res;
    }
}
