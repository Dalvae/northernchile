package com.northernchile.api.tour.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class TourScheduleRes {
    private UUID id;
    private UUID tourId;
    private String tourName;
    private Instant startDatetime;
    private Integer maxParticipants;
    private String status;
    private UUID assignedGuideId;
    private String assignedGuideName;
    private Instant createdAt;
}
