package com.northernchile.api.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BookingClientUpdateReq(
    String specialRequests,

    @NotNull(message = "Participants list is required")
    @Valid
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<ParticipantUpdateReq> participants
) {
    public record ParticipantUpdateReq(
        @NotNull(message = "Participant ID is required")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        String pickupAddress,
        String specialRequirements,
        String phoneNumber,
        String email
    ) {}
}
