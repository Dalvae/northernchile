package com.northernchile.api.tour.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class TourScheduleCreateReq {
    private UUID tourId;
    private Instant startDatetime;
    private Integer maxParticipants;
    private UUID assignedGuideId;
}
