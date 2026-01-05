package com.northernchile.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String token
) {}
