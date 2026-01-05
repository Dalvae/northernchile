package com.northernchile.api.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record TopTourReport(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String tourName,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int bookingsCount,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal revenue,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long participants
) {}
