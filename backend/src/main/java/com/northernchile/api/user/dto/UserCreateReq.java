package com.northernchile.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateReq(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @NotBlank(message = "Full name is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String fullName,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String password,

    @NotBlank(message = "Role is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String role,

    String nationality,
    String phoneNumber
) {}
