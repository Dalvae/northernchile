package com.northernchile.api.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CartItemRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID itemId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID scheduleId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID tourId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String tourName,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int numParticipants,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal pricePerParticipant,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal itemTotal,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer durationHours,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant startDatetime
) {}
