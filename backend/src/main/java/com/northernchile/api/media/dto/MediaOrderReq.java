package com.northernchile.api.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for reordering media in a gallery.
 */
public record MediaOrderReq(
    @NotNull(message = "Media ID is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    UUID mediaId,

    @NotNull(message = "Display order is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer displayOrder
) {}
