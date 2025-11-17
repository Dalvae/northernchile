package com.northernchile.api.media.dto;

import com.northernchile.api.media.model.MediaType;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Media response DTO.
 */
public class MediaRes {

    private UUID id;
    private UUID ownerId;
    private UUID tourId;
    private UUID scheduleId;
    private MediaType type;
    private String s3Key;
    private String url;
    private Map<String, String> altTranslations;
    private Map<String, String> captionTranslations;
    private String[] tags;
    private Long sizeBytes;
    private String contentType;
    private String originalFilename;
    private Map<String, String> variants;
    private Map<String, Object> exifData;
    private Instant uploadedAt;
    private Instant takenAt;

    // Additional fields for gallery context
    private Integer displayOrder;
    private Boolean isHero;
    private Boolean isInherited; // True if media is inherited from parent tour (in schedule gallery context)

    public MediaRes() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
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

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
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

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Instant getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(Instant takenAt) {
        this.takenAt = takenAt;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsHero() {
        return isHero;
    }

    public void setIsHero(Boolean isHero) {
        this.isHero = isHero;
    }

    public Boolean getIsInherited() {
        return isInherited;
    }

    public void setIsInherited(Boolean isInherited) {
        this.isInherited = isInherited;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaRes)) return false;
        MediaRes mediaRes = (MediaRes) o;
        return Objects.equals(id, mediaRes.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MediaRes{" +
                "id=" + id +
                ", type=" + type +
                ", originalFilename='" + originalFilename + '\'' +
                ", sizeBytes=" + sizeBytes +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
