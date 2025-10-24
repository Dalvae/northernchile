package com.northernchile.api.tour.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TourCreateReq {
    private String name;
    private String description;
    private String category;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;
    private Integer defaultMaxParticipants;
    private Integer durationHours;
}
