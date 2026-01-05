package com.northernchile.api.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

public record ParticipantRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String fullName,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String documentId,
    String nationality,
    LocalDate dateOfBirth,
    Integer age,
    String pickupAddress,
    String specialRequirements,
    String phoneNumber,
    String email
) {}
