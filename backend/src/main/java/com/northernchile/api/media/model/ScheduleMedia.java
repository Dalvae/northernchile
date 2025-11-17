package com.northernchile.api.media.model;

import com.northernchile.api.model.TourSchedule;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Join table entity for TourSchedule to Media relationship with ordering.
 */
@Entity
@Table(name = "schedule_media", indexes = {
    @Index(name = "idx_schedule_media_order", columnList = "schedule_id, display_order", unique = true)
})
public class ScheduleMedia implements Serializable {

    @EmbeddedId
    private ScheduleMediaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scheduleId")
    @JoinColumn(name = "schedule_id")
    private TourSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaId")
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructors
    public ScheduleMedia() {
    }

    public ScheduleMedia(TourSchedule schedule, Media media) {
        this.schedule = schedule;
        this.media = media;
        this.id = new ScheduleMediaId(schedule.getId(), media.getId());
    }

    // Getters and Setters
    public ScheduleMediaId getId() {
        return id;
    }

    public void setId(ScheduleMediaId id) {
        this.id = id;
    }

    public TourSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(TourSchedule schedule) {
        this.schedule = schedule;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleMedia)) return false;
        ScheduleMedia that = (ScheduleMedia) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Composite key for ScheduleMedia.
     */
    @Embeddable
    public static class ScheduleMediaId implements Serializable {

        @Column(name = "schedule_id")
        private UUID scheduleId;

        @Column(name = "media_id")
        private UUID mediaId;

        public ScheduleMediaId() {
        }

        public ScheduleMediaId(UUID scheduleId, UUID mediaId) {
            this.scheduleId = scheduleId;
            this.mediaId = mediaId;
        }

        public UUID getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(UUID scheduleId) {
            this.scheduleId = scheduleId;
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
            if (!(o instanceof ScheduleMediaId)) return false;
            ScheduleMediaId that = (ScheduleMediaId) o;
            return Objects.equals(scheduleId, that.scheduleId) && Objects.equals(mediaId, that.mediaId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scheduleId, mediaId);
        }
    }
}
