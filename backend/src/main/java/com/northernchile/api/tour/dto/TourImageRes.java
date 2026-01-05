package com.northernchile.api.tour.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

/**
 * DTO for tour images - mapped from Media entity
 */
public record TourImageRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String imageUrl,
    Map<String, String> variants,
    Boolean isHeroImage,
    Boolean isFeatured,
    Integer displayOrder
) {}
