
package com.northernchile.api.model;

import com.northernchile.api.audit.AuditableEntity;
import com.northernchile.api.audit.AuditEntityListener;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tour_schedules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tour_id", "start_datetime"})
})
@EntityListeners(AuditEntityListener.class)
public class TourSchedule implements AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "start_datetime", nullable = false)
    private Instant startDatetime;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TourScheduleStatus status = TourScheduleStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_guide_id")
    private User assignedGuide;

    @CreationTimestamp
    private Instant createdAt;

    public TourSchedule() {
    }

    public TourSchedule(UUID id, Tour tour, Instant startDatetime, Integer maxParticipants, TourScheduleStatus status, User assignedGuide, Instant createdAt) {
        this.id = id;
        this.tour = tour;
        this.startDatetime = startDatetime;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.assignedGuide = assignedGuide;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public Instant getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(Instant startDatetime) {
        this.startDatetime = startDatetime;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public TourScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(TourScheduleStatus status) {
        this.status = status;
    }

    public User getAssignedGuide() {
        return assignedGuide;
    }

    public void setAssignedGuide(User assignedGuide) {
        this.assignedGuide = assignedGuide;
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
        if (o == null || getClass() != o.getClass()) return false;
        TourSchedule that = (TourSchedule) o;
        return Objects.equals(id, that.id) && Objects.equals(tour, that.tour) && Objects.equals(startDatetime, that.startDatetime) && Objects.equals(maxParticipants, that.maxParticipants) && Objects.equals(status, that.status) && Objects.equals(assignedGuide, that.assignedGuide) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tour, startDatetime, maxParticipants, status, assignedGuide, createdAt);
    }

    @Override
    public String toString() {
        return "TourSchedule{" +
                "id=" + id +
                ", tour=" + tour +
                ", startDatetime=" + startDatetime +
                ", maxParticipants=" + maxParticipants +
                ", status='" + status + '\'' +
                ", assignedGuide=" + assignedGuide +
                ", createdAt=" + createdAt +
                '}';
    }

    // ==================== AuditableEntity Implementation ====================

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("America/Santiago"));

    @Override
    public String getAuditDescription() {
        String tourName = tour != null ? tour.getDisplayName() : "Unknown Tour";
        String dateStr = startDatetime != null ? DATE_FORMATTER.format(startDatetime) : "Unknown Date";
        return tourName + " @ " + dateStr;
    }

    @Override
    public String getAuditEntityType() {
        return "SCHEDULE";
    }

    @Override
    public Map<String, Object> getAuditSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("status", status);
        snapshot.put("maxParticipants", maxParticipants);
        snapshot.put("startDatetime", startDatetime != null ? startDatetime.toString() : null);
        return snapshot;
    }
}
