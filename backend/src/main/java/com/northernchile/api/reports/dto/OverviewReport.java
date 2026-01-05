package com.northernchile.api.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public record OverviewReport(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant periodStart,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant periodEnd,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long totalBookings,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long confirmedBookings,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long cancelledBookings,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalRevenue,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long totalParticipants,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal averageBookingValue,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) double conversionRate,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long totalUsers,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long totalTours,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long totalSchedules
) {}
