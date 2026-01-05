package com.northernchile.api.tour.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TourScheduleRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID tourId,
    String tourName,
    Map<String, String> tourNameTranslations,
    Integer tourDurationHours,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant startDatetime,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer maxParticipants,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer bookedParticipants,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer availableSpots,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String status,
    UUID assignedGuideId,
    String assignedGuideName,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt
) {}
