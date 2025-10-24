package com.northernchile.api.tour;

import com.northernchile.api.model.TourSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TourScheduleService {

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    public TourSchedule createScheduledTour(TourSchedule tourSchedule) {
        // Add validation logic here in the future
        return tourScheduleRepository.save(tourSchedule);
    }

    public TourSchedule cancelScheduledTour(UUID scheduleId) {
        return tourScheduleRepository.findById(scheduleId).map(schedule -> {
            schedule.setStatus("CANCELLED");
            return tourScheduleRepository.save(schedule);
        }).orElse(null); // Or throw exception
    }

    public List<TourSchedule> getScheduledToursForMonth(UUID tourId, int year, int month) {
        // This is a simplified implementation. A real implementation would
        // query by tourId, year, and month.
        return tourScheduleRepository.findAll();
    }

    public TourSchedule getTourScheduleById(UUID id) {
        return tourScheduleRepository.findById(id).orElse(null);
    }

    public void deleteTourSchedule(UUID id) {
        tourScheduleRepository.deleteById(id);
    }
}
