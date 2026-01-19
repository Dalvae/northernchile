package com.northernchile.api.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SavedParticipantRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String fullName,
    String documentId,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(type = "string", format = "date", example = "1990-05-15")
    LocalDate dateOfBirth,

    String nationality,
    String phoneNumber,
    String email,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean isSelf,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt
) {}
