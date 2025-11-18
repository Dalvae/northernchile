package com.northernchile.api.media.dto;

import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for updating media metadata.
 * Only allows updating metadata fields, not S3 references or file info.
 */
public class MediaUpdateReq {

    private Map<String, String> altTranslations;
    private Map<String, String> captionTranslations;
    private String[] tags;
    private Instant takenAt;
    private UUID tourId;
    private UUID scheduleId;

    public MediaUpdateReq() {
    }

    // Getters and Setters
    public Map<String, String> getAltTranslations() {
        return altTranslations;
    }

    public void setAltTranslations(Map<String, String> altTranslations) {
        this.altTranslations = altTranslations;
    }

    public Map<String, String> getCaptionTranslations() {
        return captionTranslations;
    }

    public void setCaptionTranslations(Map<String, String> captionTranslations) {
        this.captionTranslations = captionTranslations;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public Instant getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(Instant takenAt) {
        this.takenAt = takenAt;
    }

    public UUID getTourId() {
        return tourId;
    }

    public void setTourId(UUID tourId) {
        this.tourId = tourId;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }
}
