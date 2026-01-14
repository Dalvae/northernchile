package com.northernchile.api.tour.schedule;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.Participant;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.security.AuthorizationService;
import com.northernchile.api.security.Permission;
import com.northernchile.api.security.annotations.RequiresPermission;
import com.northernchile.api.tour.TourScheduleGeneratorService;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.tour.TourScheduleService;
import com.northernchile.api.tour.dto.TourScheduleCreateReq;
import com.northernchile.api.tour.dto.TourScheduleRes;
import com.northernchile.api.tour.schedule.dto.ScheduleParticipantsRes;
import com.northernchile.api.util.DateTimeUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/schedules")
@RequiresPermission(Permission.VIEW_SCHEDULE)
public class TourScheduleAdminController {

    private final TourScheduleRepository tourScheduleRepository;
    private final TourScheduleService tourScheduleService;
    private final TourScheduleGeneratorService generatorService;
    private final BookingRepository bookingRepository;
    private final AuthorizationService authorizationService;

    public TourScheduleAdminController(
            TourScheduleRepository tourScheduleRepository,
            TourScheduleService tourScheduleService,
            TourScheduleGeneratorService generatorService,
            BookingRepository bookingRepository,
            AuthorizationService authorizationService) {
        this.tourScheduleRepository = tourScheduleRepository;
        this.tourScheduleService = tourScheduleService;
        this.generatorService = generatorService;
        this.bookingRepository = bookingRepository;
        this.authorizationService = authorizationService;
    }

    /**
     * GET /api/admin/schedules?start=2025-11-01&end=2025-11-14&mode=future
     * Obtiene todos los schedules en un rango de fechas (para el calendario)
     * Si no se proporcionan fechas:
     *   - mode=future (default): próximos 90 días
     *   - mode=past: últimos 90 días (para asignar medios a tours pasados)
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<TourScheduleRes>> getSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false, defaultValue = "future") String mode,
            @CurrentUser User currentUser) {

        // Defaults based on mode
        LocalDate defaultStart;
        LocalDate defaultEnd;

        if ("past".equals(mode)) {
            // For media management: last 90 days
            defaultStart = start != null ? start : LocalDate.now().minusDays(90);
            defaultEnd = end != null ? end : LocalDate.now();
        } else {
            // Default (future): next 90 days
            defaultStart = start != null ? start : LocalDate.now();
            defaultEnd = end != null ? end : LocalDate.now().plusDays(90);
        }

        Instant startInstant = DateTimeUtils.toInstantStartOfDay(defaultStart);
        Instant endInstant = DateTimeUtils.toInstantEndOfDay(defaultEnd);

        List<TourSchedule> schedules = tourScheduleRepository.findByStartDatetimeBetweenWithTour(
                startInstant,
                endInstant
        );

        // Filter by ownership for Partner Admins
        if (authorizationService.isPartnerAdmin()) {
            schedules = schedules.stream()
                    .filter(schedule -> schedule.getTour() != null
                            && schedule.getTour().getOwner() != null
                            && schedule.getTour().getOwner().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        List<TourScheduleRes> response = schedules.stream()
                .map(tourScheduleService::toScheduleResWithAvailability)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/schedules
     * Crea un schedule manualmente (para David y casos especiales)
     */
    @PostMapping
    @RequiresPermission(Permission.CREATE_SCHEDULE)
    public ResponseEntity<TourScheduleRes> createSchedule(@jakarta.validation.Valid @RequestBody TourScheduleCreateReq request,
                                                          @CurrentUser User currentUser) {
        TourScheduleRes created = tourScheduleService.createScheduledTour(request, currentUser);
        return ResponseEntity.ok(created);
    }

    /**
     * PATCH /api/admin/schedules/{id}
     * Edita un schedule existente (cambiar hora, cupos, status, etc.)
     */
    @PatchMapping("/{id}")
    @RequiresPermission(value = Permission.EDIT_SCHEDULE, resourceIdParam = "id", resourceType = RequiresPermission.ResourceType.SCHEDULE)
    public ResponseEntity<TourScheduleRes> updateSchedule(
            @PathVariable String id,
            @RequestBody TourScheduleCreateReq request,
            @CurrentUser User currentUser) {
        TourScheduleRes updated = tourScheduleService.updateSchedule(UUID.fromString(id), request, currentUser);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/admin/schedules/{id}
     * Cancela un schedule (cambia status a CANCELLED)
     */
    @DeleteMapping("/{id}")
    @RequiresPermission(value = Permission.DELETE_SCHEDULE, resourceIdParam = "id", resourceType = RequiresPermission.ResourceType.SCHEDULE)
    public ResponseEntity<TourScheduleRes> cancelSchedule(
            @PathVariable String id,
            @CurrentUser User currentUser) {
        TourScheduleRes cancelled = tourScheduleService.cancelScheduledTour(UUID.fromString(id), currentUser);
        return ResponseEntity.ok(cancelled);
    }

    /**
     * POST /api/admin/schedules/generate
     * Trigger manual del generador automático (útil para testing)
     */
    @PostMapping("/generate")
    @RequiresPermission(Permission.CREATE_SCHEDULE)
    public ResponseEntity<String> generateSchedules() {
        generatorService.generateSchedulesManually();
        return ResponseEntity.ok("Schedule generation triggered successfully");
    }

    /**
     * GET /api/admin/schedules/{id}/participants
     * Obtiene todos los participantes de un schedule específico (incluye tours pasados)
     */
    @GetMapping("/{id}/participants")
    @RequiresPermission(value = Permission.VIEW_BOOKING, resourceIdParam = "id", resourceType = RequiresPermission.ResourceType.SCHEDULE)
    public ResponseEntity<ScheduleParticipantsRes> getScheduleParticipants(@PathVariable String id) {
        TourSchedule schedule = tourScheduleRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Schedule not found with id: " + id));

        List<Booking> bookings = bookingRepository.findByScheduleId(UUID.fromString(id));

        List<ScheduleParticipantsRes.ParticipantInfo> participantsList = new ArrayList<>();
        for (Booking booking : bookings) {
            for (Participant participant : booking.getParticipants()) {
                participantsList.add(new ScheduleParticipantsRes.ParticipantInfo(
                        participant.getId(),
                        participant.getFullName(),
                        participant.getDocumentId(),
                        participant.getNationality(),
                        participant.getAge(),
                        booking.getId(),
                        booking.getStatus(),
                        participant.getPickupAddress()
                ));
            }
        }

        String tourName = schedule.getTour() != null && schedule.getTour().getNameTranslations() != null
                ? schedule.getTour().getNameTranslations().get("es")
                : null;

        ScheduleParticipantsRes response = new ScheduleParticipantsRes(
                schedule.getId(),
                schedule.getStartDatetime(),
                tourName,
                schedule.getStatus(),
                bookings.size(),
                participantsList.size(),
                participantsList
        );

        return ResponseEntity.ok(response);
    }
}
