package com.northernchile.api.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for bulk assigning media to a tour or schedule.
 */
public record BulkAssignMediaReq(
    @NotNull(message = "Target ID (tour or schedule) is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    UUID targetId,

    @NotEmpty(message = "At least one media ID is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<UUID> mediaIds
) {}
