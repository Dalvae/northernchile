package com.northernchile.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String email,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String fullName,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String role,
    String nationality,
    String phoneNumber,
    LocalDate dateOfBirth,
    String documentId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String authProvider,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt
) {}
