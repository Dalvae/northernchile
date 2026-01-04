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
 * When closing a schedule, it also:
 * - Sends a manifest email to the operator
 * - Sends pickup reminder emails to all participants
 */
@Component
public class ScheduleAutoCloseJob {

    private static final Logger log = LoggerFactory.getLogger(ScheduleAutoCloseJob.class);

    private final TourScheduleRepository tourScheduleRepository;
    private final ManifestService manifestService;

    @Value("${booking.min-hours-before-tour:2}")
    private int minHoursBeforeTour;

    public ScheduleAutoCloseJob(TourScheduleRepository tourScheduleRepository, 
                                 ManifestService manifestService) {
        this.tourScheduleRepository = tourScheduleRepository;
        this.manifestService = manifestService;
    }

    /**
     * Runs every hour to close schedules that are within the cutoff window.
     * For each closed schedule, generates and sends manifest + participant reminders.
     * Interval increased to 1 hour to reduce database compute costs on Neon.
     */
    @Scheduled(fixedRate = 3600000) // Every 1 hour
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
            try {
                // Close the schedule
                schedule.setStatus("CLOSED");
                tourScheduleRepository.save(schedule);
                
                log.info("Closed schedule {} for tour {} starting at {}", 
                        schedule.getId(), 
                        schedule.getTour().getDisplayName(),
                        schedule.getStartDatetime());

                // Generate and send manifest + participant reminders
                manifestService.generateAndSendManifest(schedule);
                
            } catch (Exception e) {
                log.error("Error processing schedule {}: {}", schedule.getId(), e.getMessage(), e);
                // Continue with next schedule even if one fails
            }
        }

        log.info("Successfully auto-closed {} schedules", schedulesToClose.size());
    }
}
