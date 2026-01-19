package com.northernchile.api.tour.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.northernchile.api.util.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TourScheduleCreateReq(
    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    UUID tourId,

    // Accept either startDatetime (legacy Instant) OR date + time (preferred)
    Instant startDatetime,

    // Preferred: separate date and time fields
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(type = "string", format = "date", example = "2025-01-20")
    LocalDate date,

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(type = "string", format = "time", example = "14:30:00")
    LocalTime time,

    @NotNull
    @Min(1)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer maxParticipants,

    UUID assignedGuideId,
    String status
) {
    /**
     * Get the resolved start datetime as Instant.
     * If date + time are provided, they take precedence and are converted to Instant.
     * Otherwise, the legacy startDatetime field is used.
     */
    public Instant resolvedStartDatetime() {
        if (date != null && time != null) {
            return DateTimeUtils.toInstant(date, time);
        }
        return startDatetime;
    }
}
