package com.northernchile.api.tour.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a single item in a tour itinerary with time and description.
 * Used for structured content in tour details.
 */
public record ItineraryItem(
    @NotBlank(message = "Time is required for itinerary items")
    @JsonProperty("time")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String time,

    @NotBlank(message = "Description is required for itinerary items")
    @JsonProperty("description")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description
) {}
