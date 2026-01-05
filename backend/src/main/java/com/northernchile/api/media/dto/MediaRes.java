package com.northernchile.api.media.dto;

import com.northernchile.api.media.model.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Media response DTO.
 */
public record MediaRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID ownerId,
    UUID tourId,
    UUID scheduleId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) MediaType type,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String s3Key,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String url,
    Map<String, String> altTranslations,
    Map<String, String> captionTranslations,
    List<String> tags,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Long sizeBytes,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String contentType,
    String originalFilename,
    Map<String, String> variants,
    Map<String, Object> exifData,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant uploadedAt,
    Instant takenAt,
    // Additional fields for gallery context
    Integer displayOrder,
    Boolean isHero,
    Boolean isFeatured,
    Boolean isInherited
) {
    /**
     * Create a copy of this MediaRes with a different isInherited value.
     */
    public MediaRes withIsInherited(Boolean isInherited) {
        return new MediaRes(
            this.id, this.ownerId, this.tourId, this.scheduleId, this.type, this.s3Key, this.url,
            this.altTranslations, this.captionTranslations, this.tags, this.sizeBytes, this.contentType,
            this.originalFilename, this.variants, this.exifData, this.uploadedAt, this.takenAt,
            this.displayOrder, this.isHero, this.isFeatured, isInherited
        );
    }
}
