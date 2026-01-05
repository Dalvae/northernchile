package com.northernchile.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminPasswordChangeReq(
    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String newPassword
) {}
