package com.northernchile.api.availability;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.external.LunarService;
import com.northernchile.api.external.WeatherService;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.tour.TourScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private LunarService lunarService;

    // This DTO should be in its own file in a real application
    public static class DayAvailability {
        public String status;
        public Integer availableSlots;
        public Double moonIllumination;

        public DayAvailability(String status, Integer availableSlots, Double moonIllumination) {
            this.status = status;
            this.availableSlots = availableSlots;
            this.moonIllumination = moonIllumination;
        }
    }

    public Map<LocalDate, DayAvailability> getAvailabilityForMonth(UUID tourId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        var schedules = tourScheduleRepository.findByTourIdAndStartDatetimeBetween(
                tourId,
                startOfMonth.atStartOfDay().toInstant(ZoneOffset.UTC),
                endOfMonth.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        );

        return schedules.stream()
                .collect(Collectors.toMap(
                        schedule -> LocalDate.ofInstant(schedule.getStartDatetime(), ZoneOffset.UTC),
                        schedule -> calculateDayAvailability(schedule),
                        (day1, day2) -> day1 // In case of multiple schedules on the same day, take the first one
                ));
    }

    private DayAvailability calculateDayAvailability(TourSchedule schedule) {
        LocalDate date = LocalDate.ofInstant(schedule.getStartDatetime(), ZoneOffset.UTC);

        // Check for full moon on astronomical tours
        if ("ASTRONOMICAL".equalsIgnoreCase(schedule.getTour().getCategory()) && lunarService.isFullMoon(date)) {
            return new DayAvailability("UNAVAILABLE_MOON", 0, lunarService.getMoonIllumination(date));
        }

        // Check for strong wind on sensitive tours
        if (schedule.getTour().getIsWindSensitive() && weatherService.isWindAboveThreshold(date, 25)) {
            return new DayAvailability("UNAVAILABLE_WIND", 0, lunarService.getMoonIllumination(date));
        }

        // Calculate available slots
        Integer bookedParticipants = bookingRepository.countParticipantsByScheduleId(schedule.getId());
        int availableSlots = schedule.getMaxParticipants() - bookedParticipants;

        String status = "AVAILABLE";
        if (availableSlots <= 0) {
            status = "SOLD_OUT";
        } else if (availableSlots < 5) { // Example threshold for "few slots left"
            status = "FEW_SLOTS_LEFT";
        }

        return new DayAvailability(status, availableSlots, lunarService.getMoonIllumination(date));
    }
}
