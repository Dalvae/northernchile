package com.northernchile.api.tour;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.northernchile.api.external.LunarService;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TourScheduleGeneratorService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private LunarService lunarService;

    @Scheduled(cron = "0 0 3 * * ?") // Runs every day at 3 AM
    public void generateSchedules() {
        System.out.println("Running Tour Schedule Generator...");

        List<Tour> recurringTours = tourRepository.findByIsRecurringAndStatus(true, "PUBLISHED");
        if (recurringTours.isEmpty()) {
            System.out.println("No recurring tours to schedule.");
            return;
        }

        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));
        LocalDate today = LocalDate.now();

        for (Tour tour : recurringTours) {
            Cron cron = parser.parse(tour.getRecurrenceRule());
            ExecutionTime executionTime = ExecutionTime.forCron(cron);

            for (int i = 0; i < 90; i++) { // Check for the next 90 days
                LocalDate date = today.plusDays(i);
                ZonedDateTime zonedDateTime = date.atStartOfDay(ZoneOffset.UTC);

                Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(zonedDateTime.minusDays(1));
                if (nextExecution.isPresent() && nextExecution.get().toLocalDate().equals(date)) {
                    // CRON expression matches this day

                    // Business logic checks
                    if ("ASTRONOMICAL".equalsIgnoreCase(tour.getCategory()) && lunarService.isFullMoon(date)) {
                        continue; // Skip full moon days for astronomical tours
                    }

                    // Check if a schedule already exists for this tour on this day
                    if (!tourScheduleRepository.existsByTourIdAndStartDatetime(tour.getId(), nextExecution.get().toInstant())) {
                        TourSchedule newSchedule = new TourSchedule();
                        newSchedule.setTour(tour);
                        newSchedule.setStartDatetime(nextExecution.get().toInstant());
                        newSchedule.setMaxParticipants(tour.getDefaultMaxParticipants());
                        newSchedule.setStatus("OPEN");
                        // Guide is not assigned automatically

                        tourScheduleRepository.save(newSchedule);
                        System.out.println("Created schedule for tour '" + tour.getNameTranslations().get("es") + "' on " + date);
                    }
                }
            }
        }
        System.out.println("Tour Schedule Generator finished.");
    }
}
