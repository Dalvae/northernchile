package com.northernchile.api.media.model;

import com.northernchile.api.model.OwnedEntity;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Media entity.
 * Stores media files (photos) for tours, schedules, or as loose media.
 */
@Entity
@Table(name = "media", indexes = {
    @Index(name = "idx_media_owner", columnList = "owner_id"),
    @Index(name = "idx_media_tour", columnList = "tour_id"),
    @Index(name = "idx_media_schedule", columnList = "schedule_id"),
    @Index(name = "idx_media_uploaded_at", columnList = "uploaded_at"),
    @Index(name = "idx_media_taken_at", columnList = "taken_at")
})
public class Media implements OwnedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private TourSchedule schedule;

    @Column(name = "s3_key", nullable = false, unique = true, length = 512)
    private String s3Key;

    @Column(nullable = false, length = 1024)
    private String url;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "alt_translations", columnDefinition = "jsonb")
    private Map<String, String> altTranslations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "caption_translations", columnDefinition = "jsonb")
    private Map<String, String> captionTranslations;

    @Column(columnDefinition = "text[]")
    private String[] tags;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "original_filename", length = 512)
    private String originalFilename;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> variants;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "exif_data", columnDefinition = "jsonb")
    private Map<String, Object> exifData;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @Column(name = "taken_at")
    private Instant takenAt;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_hero")
    private Boolean isHero = false;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    // Constructors
    public Media() {
    }

    // Getters and Setters
    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public TourSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(TourSchedule schedule) {
        this.schedule = schedule;
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

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    /**
     * Determines the type of media based on associated entities.
     */
    public MediaType getType() {
        if (tour != null && schedule == null) {
            return MediaType.TOUR;
        } else if (schedule != null) {
            return MediaType.SCHEDULE;
        } else {
            return MediaType.LOOSE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Media)) return false;
        Media media = (Media) o;
        return id != null && id.equals(media.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
