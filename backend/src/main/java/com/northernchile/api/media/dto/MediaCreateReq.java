package com.northernchile.api.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating media.
 */
public record MediaCreateReq(
    UUID tourId,
    UUID scheduleId,

    @NotBlank(message = "S3 key is required")
    @Size(max = 512)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String s3Key,

    @NotBlank(message = "URL is required")
    @Size(max = 1024)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String url,

    Map<String, String> altTranslations,
    Map<String, String> captionTranslations,
    List<String> tags,

    @NotNull(message = "File size is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long sizeBytes,

    @NotBlank(message = "Content type is required")
    @Size(max = 100)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String contentType,

    @Size(max = 512)
    String originalFilename,

    Map<String, String> variants,
    Map<String, Object> exifData,
    Instant takenAt
) {}
