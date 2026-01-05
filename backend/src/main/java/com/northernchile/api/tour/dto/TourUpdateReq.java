package com.northernchile.api.tour.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public record TourUpdateReq(
    Map<String, String> nameTranslations,
    Map<String, List<ContentBlock>> descriptionBlocksTranslations,
    @JsonProperty("isMoonSensitive") Boolean isMoonSensitive,
    @JsonProperty("isWindSensitive") Boolean isWindSensitive,
    @JsonProperty("isCloudSensitive") Boolean isCloudSensitive,
    String category,
    BigDecimal price,
    Integer defaultMaxParticipants,
    Integer durationHours,
    LocalTime defaultStartTime,
    String status,
    String contentKey,
    String guideName,
    @Valid Map<String, List<@Valid ItineraryItem>> itineraryTranslations,
    @Valid Map<String, List<String>> equipmentTranslations,
    @Valid Map<String, List<String>> additionalInfoTranslations
) {}
