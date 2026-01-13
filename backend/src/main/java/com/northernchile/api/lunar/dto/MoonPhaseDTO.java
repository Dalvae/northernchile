package com.northernchile.api.lunar.dto;

import java.time.LocalDate;

/**
 * DTO for lunar phase information.
 * Used by both LunarController and CalendarDataController.
 */
public record MoonPhaseDTO(
        LocalDate date,
        double phase,           // 0.0-1.0
        int illumination,       // 0-100%
        String phaseName,       // "Full Moon", "Waxing Crescent", etc.
        boolean isFullMoon,     // true if full moon
        String icon             // Moon emoji
) {}
