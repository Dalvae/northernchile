package com.northernchile.api.booking.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class BookingCreateReq {
    private UUID scheduleId;
    private List<ParticipantReq> participants;
    private String languageCode;
    private String specialRequests;
}
