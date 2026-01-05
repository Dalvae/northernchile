package com.northernchile.api.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record CartItemReq(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID scheduleId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int numParticipants
) {}
