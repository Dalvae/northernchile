package com.northernchile.api.lunar;

import com.northernchile.api.external.LunarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para API de información lunar
 * Proporciona datos de fase lunar calculados localmente (sin límite de fechas)
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
     * Obtiene el calendario lunar para un rango de fechas
     *
     * Ejemplo:
     * GET /api/lunar/calendar?startDate=2025-01-01&endDate=2025-01-31
     *
     * Devuelve información lunar para todo enero 2025 (~30 días en 0.03ms)
     */
    @GetMapping("/calendar")
    public List<MoonPhaseDTO> getLunarCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<MoonPhaseDTO> calendar = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            double phase = lunarService.getMoonPhase(current);
            int illumination = lunarService.getMoonIllumination(current);
            String phaseName = lunarService.getMoonPhaseName(current);
            boolean isFullMoon = lunarService.isFullMoon(current);

            calendar.add(new MoonPhaseDTO(
                    current,
                    phase,
                    illumination,
                    phaseName,
                    isFullMoon,
                    lunarService.getMoonIcon(phase)
            ));

            current = current.plusDays(1);
        }

        return calendar;
    }

    /**
     * Obtiene la fase lunar para una fecha específica
     *
     * Ejemplo:
     * GET /api/lunar/phase/2025-11-15
     */
    @GetMapping("/phase/{date}")
    public MoonPhaseDTO getMoonPhaseForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        double phase = lunarService.getMoonPhase(date);
        int illumination = lunarService.getMoonIllumination(date);
        String phaseName = lunarService.getMoonPhaseName(date);
        boolean isFullMoon = lunarService.isFullMoon(date);

        return new MoonPhaseDTO(
                date,
                phase,
                illumination,
                phaseName,
                isFullMoon,
                lunarService.getMoonIcon(phase)
        );
    }

    /**
     * Obtiene las próximas N lunas llenas
     *
     * Ejemplo:
     * GET /api/lunar/next-full-moons?count=3
     *
     * Devuelve las próximas 3 lunas llenas a partir de hoy
     */
    @GetMapping("/next-full-moons")
    public List<MoonPhaseDTO> getNextFullMoons(
            @RequestParam(defaultValue = "5") int count) {

        List<MoonPhaseDTO> fullMoons = new ArrayList<>();
        LocalDate current = LocalDate.now();
        int found = 0;

        // Buscar hasta 400 días hacia el futuro (~13 meses)
        // Cada mes lunar es ~29.5 días, así que 400 días cubre suficiente
        while (found < count && current.isBefore(LocalDate.now().plusDays(400))) {
            if (lunarService.isFullMoon(current)) {
                double phase = lunarService.getMoonPhase(current);
                int illumination = lunarService.getMoonIllumination(current);
                String phaseName = lunarService.getMoonPhaseName(current);

                fullMoons.add(new MoonPhaseDTO(
                        current,
                        phase,
                        illumination,
                        phaseName,
                        true,
                        lunarService.getMoonIcon(phase)
                ));

                found++;
                // Skip at least MIN_DAYS_BETWEEN_FULL_MOONS to avoid finding the same full moon
                current = current.plusDays(MIN_DAYS_BETWEEN_FULL_MOONS);
            }
            current = current.plusDays(1);
        }

        return fullMoons;
    }


    /**
     * DTO para respuesta de fase lunar
     */
    public record MoonPhaseDTO(
            LocalDate date,
            double phase,           // 0.0-1.0
            int illumination,       // 0-100%
            String phaseName,       // "Full Moon", "Waxing Crescent", etc.
            boolean isFullMoon,     // true si es luna llena
            String icon             // Emoji de la luna
    ) {}
}
