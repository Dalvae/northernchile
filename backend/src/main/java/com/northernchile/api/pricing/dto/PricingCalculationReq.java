package com.northernchile.api.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PricingCalculationReq(
    @NotEmpty(message = "At least one item is required")
    @Valid
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<PricingItem> items
) {
    public record PricingItem(
        @NotNull(message = "Schedule ID is required")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        UUID scheduleId,

        @Min(value = 1, message = "At least 1 participant is required")
        @Max(value = 50, message = "Maximum 50 participants per item")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int numParticipants
    ) {}
}
