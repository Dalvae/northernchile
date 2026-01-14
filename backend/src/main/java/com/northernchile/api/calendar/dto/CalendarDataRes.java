package com.northernchile.api.calendar.dto;

import com.northernchile.api.lunar.dto.MoonPhaseDTO;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for calendar data endpoint.
 * Combines moon phases and weather forecast data.
 */
public record CalendarDataRes(
    List<MoonPhaseDTO> moonPhases,
    Map<String, Object> weather
) {}
