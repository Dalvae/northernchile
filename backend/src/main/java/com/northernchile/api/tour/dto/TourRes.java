package com.northernchile.api.tour.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TourRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String slug,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Map<String, String> nameTranslations,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String category,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal price,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer defaultMaxParticipants,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer durationHours,
    LocalTime defaultStartTime,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String status,
    List<TourImageRes> images,
    @JsonProperty("isMoonSensitive") boolean isMoonSensitive,
    @JsonProperty("isWindSensitive") boolean isWindSensitive,
    @JsonProperty("isCloudSensitive") boolean isCloudSensitive,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String contentKey,
    String guideName,
    List<ItineraryItem> itinerary,
    List<String> equipment,
    List<String> additionalInfo,
    Map<String, List<ItineraryItem>> itineraryTranslations,
    Map<String, List<String>> equipmentTranslations,
    Map<String, List<String>> additionalInfoTranslations,
    Map<String, List<ContentBlock>> descriptionBlocksTranslations,
    UUID ownerId,
    String ownerName,
    String ownerEmail
) {}
