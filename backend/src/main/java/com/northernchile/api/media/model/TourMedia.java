package com.northernchile.api.media.model;

import com.northernchile.api.model.Tour;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Join table entity for Tour to Media relationship with ordering.
 */
@Entity
@Table(name = "tour_media", indexes = {
    @Index(name = "idx_tour_media_order", columnList = "tour_id, display_order", unique = true),
    @Index(name = "idx_tour_media_hero", columnList = "tour_id", unique = true)
})
public class TourMedia implements Serializable {

    @EmbeddedId
    private TourMediaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tourId")
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaId")
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_hero", nullable = false)
    private Boolean isHero = false;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructors
    public TourMedia() {
    }

    public TourMedia(Tour tour, Media media) {
        this.tour = tour;
        this.media = media;
        this.id = new TourMediaId(tour.getId(), media.getId());
    }

    // Getters and Setters
    public TourMediaId getId() {
        return id;
    }

    public void setId(TourMediaId id) {
        this.id = id;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TourMedia)) return false;
        TourMedia that = (TourMedia) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Composite key for TourMedia.
     */
    @Embeddable
    public static class TourMediaId implements Serializable {

        @Column(name = "tour_id")
        private UUID tourId;

        @Column(name = "media_id")
        private UUID mediaId;

        public TourMediaId() {
        }

        public TourMediaId(UUID tourId, UUID mediaId) {
            this.tourId = tourId;
            this.mediaId = mediaId;
        }

        public UUID getTourId() {
            return tourId;
        }

        public void setTourId(UUID tourId) {
            this.tourId = tourId;
        }

        public UUID getMediaId() {
            return mediaId;
        }

        public void setMediaId(UUID mediaId) {
            this.mediaId = mediaId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TourMediaId)) return false;
            TourMediaId that = (TourMediaId) o;
            return Objects.equals(tourId, that.tourId) && Objects.equals(mediaId, that.mediaId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tourId, mediaId);
        }
    }
}
