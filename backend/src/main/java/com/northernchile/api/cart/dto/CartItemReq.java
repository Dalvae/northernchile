package com.northernchile.api.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CartItemReq(
    @NotNull(message = "Schedule ID is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    UUID scheduleId,

    @Min(value = 1, message = "At least 1 participant is required")
    @Max(value = 50, message = "Maximum 50 participants per item")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int numParticipants
) {}
