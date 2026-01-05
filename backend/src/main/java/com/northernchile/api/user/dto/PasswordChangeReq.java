package com.northernchile.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeReq(
    @NotBlank(message = "La contraseña actual es requerida")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String currentPassword,

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String newPassword
) {}
