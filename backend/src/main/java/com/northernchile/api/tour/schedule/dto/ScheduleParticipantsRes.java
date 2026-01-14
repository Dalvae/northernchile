package com.northernchile.api.tour.schedule.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for schedule participants list.
 */
public record ScheduleParticipantsRes(
    UUID scheduleId,
    Instant startDatetime,
    String tourName,
    String status,
    int totalBookings,
    int totalParticipants,
    List<ParticipantInfo> participants
) {
    public record ParticipantInfo(
        UUID participantId,
        String fullName,
        String documentId,
        String nationality,
        Integer age,
        UUID bookingId,
        String bookingStatus,
        String pickupAddress
    ) {}
}
