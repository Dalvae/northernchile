package com.northernchile.api.booking.dto;

import com.northernchile.api.model.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record BookingUpdateReq(
    @NotNull(message = "Status is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    BookingStatus status,

    String specialRequests
) {}
