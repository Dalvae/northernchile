package com.northernchile.api.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record BookingUpdateReq(
    @NotBlank(message = "Status is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String status,

    String specialRequests
) {}
