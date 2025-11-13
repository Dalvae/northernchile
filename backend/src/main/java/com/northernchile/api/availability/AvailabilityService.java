package com.northernchile.api.availability;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.external.LunarService;
import com.northernchile.api.external.WeatherService;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.tour.TourScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private static final int WIND_THRESHOLD_KNOTS = 25;
    private static final int FEW_SLOTS_THRESHOLD = 5;
    private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");

    private final TourScheduleRepository tourScheduleRepository;
    private final AvailabilityValidator availabilityValidator;
    private final WeatherService weatherService;
    private final LunarService lunarService;

    public AvailabilityService(
            TourScheduleRepository tourScheduleRepository,
            AvailabilityValidator availabilityValidator,
            WeatherService weatherService,
            LunarService lunarService) {
        this.tourScheduleRepository = tourScheduleRepository;
        this.availabilityValidator = availabilityValidator;
        this.weatherService = weatherService;
        this.lunarService = lunarService;
    }

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
                startOfMonth.atStartOfDay(ZONE_ID).toInstant(),
                endOfMonth.plusDays(1).atStartOfDay(ZONE_ID).toInstant()
        );

        return schedules.stream()
                .collect(Collectors.toMap(
                        schedule -> LocalDate.ofInstant(schedule.getStartDatetime(), ZONE_ID),
                        schedule -> calculateDayAvailability(schedule),
                        (day1, day2) -> day1 // In case of multiple schedules on the same day, take the first one
                ));
    }

    private DayAvailability calculateDayAvailability(TourSchedule schedule) {
        LocalDate date = LocalDate.ofInstant(schedule.getStartDatetime(), ZONE_ID);

        // Check for full moon on astronomical tours
        if ("ASTRONOMICAL".equalsIgnoreCase(schedule.getTour().getCategory()) && lunarService.isFullMoon(date)) {
            return new DayAvailability("UNAVAILABLE_MOON", Integer.valueOf(0), Double.valueOf(lunarService.getMoonIllumination(date)));
        }

        // Check for strong wind on sensitive tours
        if (schedule.getTour().isWindSensitive() && weatherService.isWindAboveThreshold(date, WIND_THRESHOLD_KNOTS)) {
            return new DayAvailability("UNAVAILABLE_WIND", Integer.valueOf(0), Double.valueOf(lunarService.getMoonIllumination(date)));
        }

        // Get availability status using centralized validator (accounts for both bookings and carts)
        var availabilityStatus = availabilityValidator.getAvailabilityStatus(
                schedule.getId(),
                schedule.getMaxParticipants()
        );
        Integer availableSlots = availabilityStatus.getAvailableSlots();

        String status = "AVAILABLE";
        if (availableSlots <= 0) {
            status = "SOLD_OUT";
        } else if (availableSlots < FEW_SLOTS_THRESHOLD) {
            status = "FEW_SLOTS_LEFT";
        }

        return new DayAvailability(status, availableSlots, Double.valueOf(lunarService.getMoonIllumination(date)));
    }
}
