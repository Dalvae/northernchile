package com.northernchile.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(type = "string", format = "date", example = "1990-05-15")
    LocalDate dateOfBirth
) {}
