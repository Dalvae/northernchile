package com.northernchile.api.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record PricingCalculationReq(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<PricingItem> items
) {
    public record PricingItem(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID scheduleId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int numParticipants
    ) {}
}
