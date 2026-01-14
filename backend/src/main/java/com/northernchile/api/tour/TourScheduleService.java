package com.northernchile.api.tour;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.availability.AvailabilityValidator;
import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourScheduleCreateReq;
import com.northernchile.api.tour.dto.TourScheduleRes;
import com.northernchile.api.tour.mapper.TourScheduleMapper;
import com.northernchile.api.user.UserRepository;
import com.northernchile.api.security.AuthorizationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TourScheduleService {

    private final TourScheduleRepository tourScheduleRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final AvailabilityValidator availabilityValidator;
    private final AuditLogService auditLogService;
    private final AuthorizationService authorizationService;
    private final TourScheduleMapper tourScheduleMapper;

    public TourScheduleService(
            TourScheduleRepository tourScheduleRepository,
            TourRepository tourRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            AvailabilityValidator availabilityValidator,
            AuditLogService auditLogService,
            AuthorizationService authorizationService,
            TourScheduleMapper tourScheduleMapper) {
        this.tourScheduleRepository = tourScheduleRepository;
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityValidator = availabilityValidator;
        this.auditLogService = auditLogService;
        this.authorizationService = authorizationService;
        this.tourScheduleMapper = tourScheduleMapper;
    }

    @Transactional
    public TourScheduleRes createScheduledTour(TourScheduleCreateReq req, User currentUser) {
        Tour tour = tourRepository.findByIdNotDeleted(req.tourId())
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + req.tourId()));

        // Check ownership for non-super-admins
        authorizationService.checkOwnership(tour, "You do not have permission to create schedules for this tour.");

        var guide = req.assignedGuideId() != null ? userRepository.findById(req.assignedGuideId())
                .orElseThrow(() -> new EntityNotFoundException("Guide not found with id: " + req.assignedGuideId())) : null;

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);
        schedule.setStartDatetime(req.resolvedStartDatetime());
        schedule.setMaxParticipants(req.maxParticipants());
        schedule.setAssignedGuide(guide);
        schedule.setStatus("OPEN");

        TourSchedule savedSchedule = tourScheduleRepository.save(schedule);

        // Audit log
        String tourName = tour.getDisplayName();
        String description = tourName + " - " + savedSchedule.getStartDatetime().toString();
        Map<String, Object> newValues = Map.of(
            "id", savedSchedule.getId().toString(),
            "tourId", tour.getId().toString(),
            "tourName", tourName,
            "startDatetime", savedSchedule.getStartDatetime().toString(),
            "status", savedSchedule.getStatus()
        );
        auditLogService.logCreate(currentUser, "SCHEDULE", savedSchedule.getId(), description, newValues);

        return toTourScheduleRes(savedSchedule);
    }

    @Transactional
    public TourScheduleRes updateSchedule(UUID scheduleId, TourScheduleCreateReq req, User currentUser) {
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found with id: " + scheduleId));

        Tour tour = schedule.getTour();

        // Check ownership for non-super-admins
        authorizationService.checkOwnership(tour, "You do not have permission to update this schedule.");

        // Capture old values for audit
        Map<String, Object> oldValues = Map.of(
            "startDatetime", schedule.getStartDatetime().toString(),
            "maxParticipants", schedule.getMaxParticipants(),
            "status", schedule.getStatus()
        );

        // Update fields
        if (req.startDatetime() != null || (req.date() != null && req.time() != null)) {
            schedule.setStartDatetime(req.resolvedStartDatetime());
        }
        if (req.maxParticipants() != null) {
            schedule.setMaxParticipants(req.maxParticipants());
        }
        if (req.status() != null) {
            schedule.setStatus(req.status());
        }

        TourSchedule savedSchedule = tourScheduleRepository.save(schedule);

        // Audit log
        String tourName = tour.getDisplayName();
        String description = tourName + " - " + savedSchedule.getStartDatetime().toString();
        Map<String, Object> newValues = Map.of(
            "startDatetime", savedSchedule.getStartDatetime().toString(),
            "maxParticipants", savedSchedule.getMaxParticipants(),
            "status", savedSchedule.getStatus()
        );
        auditLogService.logUpdate(currentUser, "SCHEDULE", schedule.getId(), description, oldValues, newValues);

        return toTourScheduleRes(savedSchedule);
    }

    @Transactional
    public TourScheduleRes cancelScheduledTour(UUID scheduleId, User currentUser) {
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found with id: " + scheduleId));

        Tour tour = schedule.getTour();

        // Check ownership for non-super-admins
        authorizationService.checkOwnership(tour, "You do not have permission to cancel this schedule.");

        // Capture old status for audit
        String oldStatus = schedule.getStatus();

        schedule.setStatus("CANCELLED");
        TourSchedule savedSchedule = tourScheduleRepository.save(schedule);

        // Audit log
        String tourName = tour.getDisplayName();
        String description = tourName + " - " + savedSchedule.getStartDatetime().toString();
        Map<String, Object> oldValues = Map.of("status", oldStatus);
        Map<String, Object> newValues = Map.of("status", "CANCELLED");
        auditLogService.logUpdate(currentUser, "SCHEDULE", schedule.getId(), description, oldValues, newValues);

        return toTourScheduleRes(savedSchedule);
    }

    public List<TourScheduleRes> getScheduledToursForMonth(UUID tourId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        var startOfMonth = yearMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        var endOfMonth = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        return tourScheduleRepository.findByTourIdAndStartDatetimeBetweenOrderByStartDatetimeDesc(tourId, startOfMonth, endOfMonth).stream()
                .map(this::toTourScheduleRes)
                .collect(Collectors.toList());
    }

    public TourScheduleRes getTourScheduleById(UUID id) {
        return tourScheduleRepository.findById(id)
                .map(this::toTourScheduleRes)
                .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found with id: " + id));
    }

    @Transactional
    public void deleteTourSchedule(UUID id, User currentUser) {
        TourSchedule schedule = tourScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found with id: " + id));

        Tour tour = schedule.getTour();

        // Check ownership for non-super-admins
        authorizationService.checkOwnership(tour, "You do not have permission to delete this schedule.");

        // Audit log before deletion
        String tourName = tour.getDisplayName();
        String description = tourName + " - " + schedule.getStartDatetime().toString();
        Map<String, Object> oldValues = Map.of(
            "tourId", tour.getId().toString(),
            "tourName", tourName,
            "startDatetime", schedule.getStartDatetime().toString(),
            "status", schedule.getStatus()
        );
        auditLogService.logDelete(currentUser, "SCHEDULE", schedule.getId(), description, oldValues);

        tourScheduleRepository.deleteById(id);
    }

    private TourScheduleRes toTourScheduleRes(TourSchedule schedule) {
        // Get availability status (accounts for both confirmed bookings and cart reservations)
        var availabilityStatus = availabilityValidator.getAvailabilityStatus(
                schedule.getId(),
                schedule.getMaxParticipants()
        );
        int totalReserved = schedule.getMaxParticipants() - availabilityStatus.availableSlots();

        return tourScheduleMapper.toRes(schedule, totalReserved, availabilityStatus.availableSlots());
    }

    /**
     * Converts a TourSchedule to TourScheduleRes with availability calculation.
     * Uses confirmed bookings only (excludes cart reservations).
     *
     * @param schedule the tour schedule entity
     * @return TourScheduleRes with calculated availability
     */
    public TourScheduleRes toScheduleResWithAvailability(TourSchedule schedule) {
        Integer bookedParticipants = bookingRepository.countConfirmedParticipantsByScheduleId(schedule.getId());
        if (bookedParticipants == null) {
            bookedParticipants = 0;
        }
        int availableSpots = Math.max(0, schedule.getMaxParticipants() - bookedParticipants);

        return tourScheduleMapper.toRes(schedule, bookedParticipants, availableSpots);
    }
}
