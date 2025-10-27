package com.northernchile.api.tour;

import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.tour.dto.TourScheduleCreateReq;
import com.northernchile.api.tour.dto.TourScheduleRes;
import com.northernchile.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
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

    public TourScheduleRes createScheduledTour(TourScheduleCreateReq req) {
        var tour = tourRepository.findById(req.getTourId())
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + req.getTourId()));

        var guide = req.getAssignedGuideId() != null ? userRepository.findById(req.getAssignedGuideId())
                .orElseThrow(() -> new EntityNotFoundException("Guide not found with id: " + req.getAssignedGuideId())) : null;

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);
        schedule.setStartDatetime(req.getStartDatetime());
        schedule.setMaxParticipants(req.getMaxParticipants());
        schedule.setAssignedGuide(guide);
        schedule.setStatus("OPEN");

        TourSchedule savedSchedule = tourScheduleRepository.save(schedule);
        return toTourScheduleRes(savedSchedule);
    }

    public TourScheduleRes cancelScheduledTour(UUID scheduleId) {
        return tourScheduleRepository.findById(scheduleId).map(schedule -> {
            schedule.setStatus("CANCELLED");
            TourSchedule savedSchedule = tourScheduleRepository.save(schedule);
            return toTourScheduleRes(savedSchedule);
        }).orElseThrow(() -> new EntityNotFoundException("TourSchedule not found with id: " + scheduleId));
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

    public void deleteTourSchedule(UUID id) {
        if (!tourScheduleRepository.existsById(id)) {
            throw new EntityNotFoundException("TourSchedule not found with id: " + id);
        }
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
