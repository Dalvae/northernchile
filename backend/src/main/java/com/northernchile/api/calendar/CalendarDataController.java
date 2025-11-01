package com.northernchile.api.calendar;

import com.northernchile.api.external.LunarService;
import com.northernchile.api.external.WeatherService;
import com.northernchile.api.lunar.LunarController.MoonPhaseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint combinado para datos del calendario
 * Devuelve fases lunares + pronóstico meteorológico en una sola llamada
 */
@RestController
@RequestMapping("/api/calendar")
public class CalendarDataController {

    private final LunarService lunarService;
    private final WeatherService weatherService;

    public CalendarDataController(LunarService lunarService, WeatherService weatherService) {
        this.lunarService = lunarService;
        this.weatherService = weatherService;
    }

    /**
     * Obtiene todos los datos necesarios para el calendario en una sola llamada
     *
     * GET /api/calendar/data?startDate=2025-01-01&endDate=2025-01-31
     *
     * Retorna:
     * {
     *   "moonPhases": [ ... ], // Array de fases lunares para el rango solicitado
     *   "weather": {           // Pronóstico de 5 días (cacheado 3 horas)
     *     "daily": [ ... ]
     *   }
     * }
     */
    @GetMapping("/data")
    public Map<String, Object> getCalendarData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // 1. Calcular fases lunares (sin límite, calculado localmente)
        List<MoonPhaseDTO> moonPhases = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            double phase = lunarService.getMoonPhase(current);
            int illumination = lunarService.getMoonIllumination(current);
            String phaseName = lunarService.getMoonPhaseName(current);
            boolean isFullMoon = lunarService.isFullMoon(current);

            moonPhases.add(new MoonPhaseDTO(
                    current,
                    phase,
                    illumination,
                    phaseName,
                    isFullMoon,
                    getMoonIcon(phase)
            ));

            current = current.plusDays(1);
        }

        // 2. Obtener pronóstico meteorológico (5 días, cacheado 3 horas)
        Map<String, Object> weatherForecast = weatherService.getForecast();

        // 3. Combinar ambos en una sola respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("moonPhases", moonPhases);
        response.put("weather", weatherForecast);

        return response;
    }

    /**
     * Mapea la fase lunar a un icono/emoji
     */
    private String getMoonIcon(double phase) {
        if (phase < 0.03 || phase > 0.97) return "🌑"; // New Moon
        if (phase >= 0.03 && phase < 0.22) return "🌒"; // Waxing Crescent
        if (phase >= 0.22 && phase < 0.28) return "🌓"; // First Quarter
        if (phase >= 0.28 && phase < 0.47) return "🌔"; // Waxing Gibbous
        if (phase >= 0.47 && phase < 0.53) return "🌕"; // Full Moon
        if (phase >= 0.53 && phase < 0.72) return "🌖"; // Waning Gibbous
        if (phase >= 0.72 && phase < 0.78) return "🌗"; // Last Quarter
        if (phase >= 0.78 && phase <= 0.97) return "🌘"; // Waning Crescent
        return "🌑";
    }

    /**
     * DTO para respuesta de fase lunar (copiado de LunarController)
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
