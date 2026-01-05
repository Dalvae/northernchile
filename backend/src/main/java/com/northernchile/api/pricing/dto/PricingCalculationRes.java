package com.northernchile.api.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PricingCalculationRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<PricingLineItem> items,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal subtotal,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal taxAmount,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal taxRate,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalAmount
) {
    public record PricingLineItem(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID scheduleId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID tourId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String tourName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int numParticipants,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal pricePerParticipant,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal lineTotal
    ) {}
}
