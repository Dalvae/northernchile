package com.northernchile.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterReq(
    @NotBlank
    @Email
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String password,

    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String fullName,

    String nationality,
    String phoneNumber,
    LocalDate dateOfBirth
) {}
