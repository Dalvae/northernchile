package com.northernchile.api.availability;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.external.LunarService;
import com.northernchile.api.external.WeatherService;
import com.northernchile.api.tour.TourScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        // This is a placeholder implementation. A real implementation would be much more complex.
        // It would need to:
        // 1. Get all TourSchedules for the given tourId, year, and month.
        // 2. For each schedule, calculate the remaining slots by querying the BookingRepository.
        // 3. Get the weather forecast for the month.
        // 4. For each day, get the lunar phase.
        // 5. Combine all this information to determine the status of each day.

        Map<LocalDate, DayAvailability> availabilityMap = new HashMap<>();
        LocalDate date = LocalDate.of(year, month, 1);
        availabilityMap.put(date, new DayAvailability("AVAILABLE", 10, lunarService.getMoonIllumination(date)));
        availabilityMap.put(date.plusDays(1), new DayAvailability("SOLD_OUT", 0, lunarService.getMoonIllumination(date.plusDays(1))));
        availabilityMap.put(date.plusDays(2), new DayAvailability("UNAVAILABLE_MOON", 0, lunarService.getMoonIllumination(date.plusDays(2))));

        return availabilityMap;
    }
}
