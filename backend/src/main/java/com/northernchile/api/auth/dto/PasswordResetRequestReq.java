package com.northernchile.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestReq(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String email
) {}
