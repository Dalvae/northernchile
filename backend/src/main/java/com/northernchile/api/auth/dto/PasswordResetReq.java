package com.northernchile.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetReq(
    @NotBlank(message = "Token is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String token,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String newPassword
) {}
