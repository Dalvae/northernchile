package com.northernchile.api.tour;

import com.northernchile.api.model.TourSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduled job that automatically closes tour schedules
 * when they are within the booking cutoff window.
 * 
 * This prevents last-minute bookings that could cause operational issues.
 */
@Component
public class ScheduleAutoCloseJob {

    private static final Logger log = LoggerFactory.getLogger(ScheduleAutoCloseJob.class);

    private final TourScheduleRepository tourScheduleRepository;

    @Value("${booking.min-hours-before-tour:2}")
    private int minHoursBeforeTour;

    public ScheduleAutoCloseJob(TourScheduleRepository tourScheduleRepository) {
        this.tourScheduleRepository = tourScheduleRepository;
    }

    /**
     * Runs every 5 minutes to close schedules that are within the cutoff window.
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void closeSchedulesWithinCutoff() {
        Instant cutoffTime = Instant.now().plus(minHoursBeforeTour, ChronoUnit.HOURS);
        
        List<TourSchedule> schedulesToClose = tourScheduleRepository
                .findOpenSchedulesBeforeCutoff(cutoffTime);

        if (schedulesToClose.isEmpty()) {
            return;
        }

        log.info("Auto-closing {} schedules within {} hour cutoff", 
                schedulesToClose.size(), minHoursBeforeTour);

        for (TourSchedule schedule : schedulesToClose) {
            schedule.setStatus("CLOSED");
            tourScheduleRepository.save(schedule);
            log.debug("Closed schedule {} for tour {} starting at {}", 
                    schedule.getId(), 
                    schedule.getTour().getDisplayName(),
                    schedule.getStartDatetime());
        }

        log.info("Successfully auto-closed {} schedules", schedulesToClose.size());
    }
}
