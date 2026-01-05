package com.northernchile.api.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BookingCreateReq(
    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    UUID scheduleId,

    @NotEmpty
    @Valid
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<ParticipantReq> participants,

    String languageCode,
    String specialRequests
) {}
