package com.northernchile.api.tour.schedule;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.booking.dto.ParticipantRes;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.Participant;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.TourScheduleGeneratorService;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.tour.TourScheduleService;
import com.northernchile.api.tour.dto.TourScheduleCreateReq;
import com.northernchile.api.tour.dto.TourScheduleRes;
import com.northernchile.api.user.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/schedules")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
public class TourScheduleAdminController {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");

    private final TourScheduleRepository tourScheduleRepository;
    private final TourScheduleService tourScheduleService;
    private final TourScheduleGeneratorService generatorService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public TourScheduleAdminController(
            TourScheduleRepository tourScheduleRepository,
            TourScheduleService tourScheduleService,
            TourScheduleGeneratorService generatorService,
            UserRepository userRepository,
            BookingRepository bookingRepository) {
        this.tourScheduleRepository = tourScheduleRepository;
        this.tourScheduleService = tourScheduleService;
        this.generatorService = generatorService;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userRepository.findByEmail(currentPrincipalName)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentPrincipalName));
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
        User currentUser = getCurrentUser();
        TourScheduleRes created = tourScheduleService.createScheduledTour(request, currentUser);
        return ResponseEntity.ok(created);
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

    /**
     * GET /api/admin/schedules/{id}/participants
     * Obtiene todos los participantes de un schedule específico (incluye tours pasados)
     */
    @GetMapping("/{id}/participants")
    public ResponseEntity<Map<String, Object>> getScheduleParticipants(@PathVariable String id) {
        TourSchedule schedule = tourScheduleRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Schedule not found: " + id));

        // Buscar todas las reservas confirmadas para este schedule
        List<Booking> bookings = bookingRepository.findByTourScheduleId(UUID.fromString(id));

        // Extraer todos los participantes
        List<Map<String, Object>> participantsList = new ArrayList<>();
        for (Booking booking : bookings) {
            for (Participant participant : booking.getParticipants()) {
                Map<String, Object> participantData = new HashMap<>();
                participantData.put("participantId", participant.getId());
                participantData.put("fullName", participant.getFullName());
                participantData.put("documentId", participant.getDocumentId());
                participantData.put("nationality", participant.getNationality());
                participantData.put("age", participant.getAge());
                participantData.put("type", participant.getType());
                participantData.put("bookingId", booking.getId());
                participantData.put("bookingStatus", booking.getStatus());
                participantData.put("bookingReference", booking.getBookingReference());
                participantData.put("pickupAddress", booking.getPickupAddress());
                participantsList.add(participantData);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", schedule.getId());
        response.put("startDatetime", schedule.getStartDatetime());
        response.put("tourName", schedule.getTour() != null ? schedule.getTour().getName() : null);
        response.put("status", schedule.getStatus());
        response.put("totalBookings", bookings.size());
        response.put("totalParticipants", participantsList.size());
        response.put("participants", participantsList);

        return ResponseEntity.ok(response);
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
