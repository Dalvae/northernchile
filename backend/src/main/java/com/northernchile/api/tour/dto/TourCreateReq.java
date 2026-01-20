package com.northernchile.api.tour.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.northernchile.api.model.TourStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public record TourCreateReq(
    @NotEmpty
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> nameTranslations,

    @NotEmpty
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, List<ContentBlock>> descriptionBlocksTranslations,

    @JsonProperty("isWindSensitive")
    Boolean isWindSensitive,

    @JsonProperty("isMoonSensitive")
    Boolean isMoonSensitive,

    @JsonProperty("isCloudSensitive")
    Boolean isCloudSensitive,

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String category,

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal price,

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer defaultMaxParticipants,

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer durationHours,

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(type = "string", format = "time", example = "14:30:00")
    LocalTime defaultStartTime,

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    TourStatus status,

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String contentKey,

    String guideName,

    @Valid
    Map<String, List<@Valid ItineraryItem>> itineraryTranslations,

    @Valid
    Map<String, List<String>> equipmentTranslations,

    @Valid
    Map<String, List<String>> additionalInfoTranslations
) {}
