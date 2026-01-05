package com.northernchile.api.media.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for updating media metadata.
 * Only allows updating metadata fields, not S3 references or file info.
 */
public record MediaUpdateReq(
    Map<String, String> altTranslations,
    Map<String, String> captionTranslations,
    List<String> tags,
    Instant takenAt,
    UUID tourId,
    UUID scheduleId
) {}
