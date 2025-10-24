package com.northernchile.api.tour.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class TourRes {
    private UUID id;
    private String name;
    private String description;
    private String category;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;
    private Integer defaultMaxParticipants;
    private Integer durationHours;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
