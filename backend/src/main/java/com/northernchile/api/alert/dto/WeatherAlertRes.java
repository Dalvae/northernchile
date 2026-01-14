package com.northernchile.api.alert.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for weather alerts with computed fields for the frontend
 */
public record WeatherAlertRes(
        UUID id,
        String alertType,
        String severity,
        String status,
        String message,
        Double windSpeed,
        Integer cloudCoverage,
        Double moonPhase,
        String resolution,
        String resolvedBy,
        Instant createdAt,
        Instant resolvedAt,
        // Computed fields from TourSchedule
        UUID tourScheduleId,
        String title,
        String description,
        Instant scheduleDate,
        String tourName,
        // Computed moon illumination (percentage)
        Integer moonIllumination
) {}
