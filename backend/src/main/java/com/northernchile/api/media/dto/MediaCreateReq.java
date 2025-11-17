package com.northernchile.api.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating media.
 */
public class MediaCreateReq {

    private UUID tourId;
    private UUID scheduleId;

    @NotBlank(message = "S3 key is required")
    @Size(max = 512)
    private String s3Key;

    @NotBlank(message = "URL is required")
    @Size(max = 1024)
    private String url;

    private Map<String, String> altTranslations;
    private Map<String, String> captionTranslations;
    private String[] tags;

    @NotNull(message = "File size is required")
    private Long sizeBytes;

    @NotBlank(message = "Content type is required")
    @Size(max = 100)
    private String contentType;

    @Size(max = 512)
    private String originalFilename;

    private Map<String, String> variants;
    private Map<String, Object> exifData;
    private Instant takenAt;

    public MediaCreateReq() {
    }

    // Getters and Setters
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

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

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

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Map<String, String> getVariants() {
        return variants;
    }

    public void setVariants(Map<String, String> variants) {
        this.variants = variants;
    }

    public Map<String, Object> getExifData() {
        return exifData;
    }

    public void setExifData(Map<String, Object> exifData) {
        this.exifData = exifData;
    }

    public Instant getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(Instant takenAt) {
        this.takenAt = takenAt;
    }
}
