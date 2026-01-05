package com.northernchile.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginReq(
    @NotBlank
    @Email
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {}
