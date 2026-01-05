package com.northernchile.api.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record BookingRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
    UUID userId,
    String userFullName,
    String userPhoneNumber,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID scheduleId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String tourName,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate tourDate,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalTime tourStartTime,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String status,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal subtotal,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal taxAmount,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalAmount,
    String languageCode,
    String specialRequests,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<ParticipantRes> participants
) {}
