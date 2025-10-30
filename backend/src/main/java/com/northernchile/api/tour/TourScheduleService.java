package com.northernchile.api.tour;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourScheduleCreateReq;
import com.northernchile.api.tour.dto.TourScheduleRes;
import com.northernchile.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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

    @Autowired
    private TourScheduleRepository tourScheduleRepository;
    @Autowired
    private TourRepository tourRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public TourScheduleRes createScheduledTour(TourScheduleCreateReq req, User currentUser) {
        Tour tour = tourRepository.findByIdNotDeleted(req.getTourId())
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + req.getTourId()));

        // Check ownership for non-super-admins
        if (!"ROLE_SUPER_ADMIN".equals(currentUser.getRole()) &&
            !tour.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to create schedules for this tour.");
        }

        var guide = req.getAssignedGuideId() != null ? userRepository.findById(req.getAssignedGuideId())
                .orElseThrow(() -> new EntityNotFoundException("Guide not found with id: " + req.getAssignedGuideId())) : null;

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);
        schedule.setStartDatetime(req.getStartDatetime());
        schedule.setMaxParticipants(req.getMaxParticipants());
        schedule.setAssignedGuide(guide);
        schedule.setStatus("OPEN");

        TourSchedule savedSchedule = tourScheduleRepository.save(schedule);

        // Audit log
        String tourName = tour.getNameTranslations().getOrDefault("es", "Tour sin nombre");
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
    public TourScheduleRes cancelScheduledTour(UUID scheduleId, User currentUser) {
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found with id: " + scheduleId));

        Tour tour = schedule.getTour();

        // Check ownership for non-super-admins
        if (!"ROLE_SUPER_ADMIN".equals(currentUser.getRole()) &&
            !tour.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to cancel this schedule.");
        }

        // Capture old status for audit
        String oldStatus = schedule.getStatus();

        schedule.setStatus("CANCELLED");
        TourSchedule savedSchedule = tourScheduleRepository.save(schedule);

        // Audit log
        String tourName = tour.getNameTranslations().getOrDefault("es", "Tour sin nombre");
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

        return tourScheduleRepository.findByTourIdAndStartDatetimeBetween(tourId, startOfMonth, endOfMonth).stream()
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
        if (!"ROLE_SUPER_ADMIN".equals(currentUser.getRole()) &&
            !tour.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this schedule.");
        }

        // Audit log before deletion
        String tourName = tour.getNameTranslations().getOrDefault("es", "Tour sin nombre");
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
        TourScheduleRes res = new TourScheduleRes();
        res.setId(schedule.getId());
        res.setTourId(schedule.getTour().getId());
        res.setTourName(schedule.getTour().getNameTranslations().get("es"));
        res.setStartDatetime(schedule.getStartDatetime());
        res.setMaxParticipants(schedule.getMaxParticipants());
        res.setStatus(schedule.getStatus());
        if (schedule.getAssignedGuide() != null) {
            res.setAssignedGuideId(schedule.getAssignedGuide().getId());
            res.setAssignedGuideName(schedule.getAssignedGuide().getFullName());
        }
        res.setCreatedAt(schedule.getCreatedAt());
        return res;
    }
}
