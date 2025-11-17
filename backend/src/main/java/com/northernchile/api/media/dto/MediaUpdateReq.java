package com.northernchile.api.media.dto;

import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

/**
 * Request DTO for updating media metadata.
 * Only allows updating metadata fields, not S3 references or file info.
 */
public class MediaUpdateReq {

    private Map<String, String> altTranslations;
    private Map<String, String> captionTranslations;
    private String[] tags;
    private Instant takenAt;

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
}
