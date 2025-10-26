
package com.northernchile.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tour_schedules")
public class TourSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(nullable = false)
    private Instant startDatetime;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(length = 20)
    private String status = "OPEN";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_guide_id")
    private User assignedGuide;

    @CreationTimestamp
    private Instant createdAt;

    public TourSchedule() {
    }

    public TourSchedule(UUID id, Tour tour, Instant startDatetime, Integer maxParticipants, String status, User assignedGuide, Instant createdAt) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
                ", status='" + status + ''' +
                ", assignedGuide=" + assignedGuide +
                ", createdAt=" + createdAt +
                '}';
    }
}
