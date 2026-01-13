package com.northernchile.api.lunar;

import com.northernchile.api.external.LunarService;
import com.northernchile.api.lunar.dto.MoonPhaseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for lunar information API.
 * Provides lunar phase data calculated locally (no date limits).
 */
@RestController
@RequestMapping("/api/lunar")
public class LunarController {

    private static final int MIN_DAYS_BETWEEN_FULL_MOONS = 25;

    private final LunarService lunarService;

    public LunarController(LunarService lunarService) {
        this.lunarService = lunarService;
    }

    /**
     * Get lunar calendar for a date range.
     *
     * Example:
     * GET /api/lunar/calendar?startDate=2025-01-01&endDate=2025-01-31
     *
     * Returns lunar information for all of January 2025 (~30 days in 0.03ms)
     */
    @GetMapping("/calendar")
    public List<MoonPhaseDTO> getLunarCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return lunarService.getMoonPhasesForRange(startDate, endDate);
    }

    /**
     * Get lunar phase for a specific date.
     *
     * Example:
     * GET /api/lunar/phase/2025-11-15
     */
    @GetMapping("/phase/{date}")
    public MoonPhaseDTO getMoonPhaseForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return lunarService.getMoonPhaseData(date);
    }

    /**
     * Get the next N full moons.
     *
     * Example:
     * GET /api/lunar/next-full-moons?count=3
     *
     * Returns the next 3 full moons starting from today.
     */
    @GetMapping("/next-full-moons")
    public List<MoonPhaseDTO> getNextFullMoons(
            @RequestParam(defaultValue = "5") int count) {

        List<MoonPhaseDTO> fullMoons = new ArrayList<>();
        LocalDate current = LocalDate.now();
        int found = 0;

        // Search up to 400 days in the future (~13 months)
        // Each lunar cycle is ~29.5 days, so 400 days covers enough
        while (found < count && current.isBefore(LocalDate.now().plusDays(400))) {
            if (lunarService.isFullMoon(current)) {
                fullMoons.add(lunarService.getMoonPhaseData(current));
                found++;
                // Skip at least MIN_DAYS_BETWEEN_FULL_MOONS to avoid finding the same full moon
                current = current.plusDays(MIN_DAYS_BETWEEN_FULL_MOONS);
            }
            current = current.plusDays(1);
        }

        return fullMoons;
    }
}
