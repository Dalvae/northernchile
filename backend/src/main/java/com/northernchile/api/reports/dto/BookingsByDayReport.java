package com.northernchile.api.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingsByDayReport(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate date,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int count,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal revenue
) {}
