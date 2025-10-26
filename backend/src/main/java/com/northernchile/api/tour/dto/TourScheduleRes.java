
package com.northernchile.api.tour.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TourScheduleRes {
    private UUID id;
    private UUID tourId;
    private String tourName;
    private Instant startDatetime;
    private Integer maxParticipants;
    private String status;
    private UUID assignedGuideId;
    private String assignedGuideName;
    private Instant createdAt;

    public TourScheduleRes() {
    }

    public TourScheduleRes(UUID id, UUID tourId, String tourName, Instant startDatetime, Integer maxParticipants, String status, UUID assignedGuideId, String assignedGuideName, Instant createdAt) {
        this.id = id;
        this.tourId = tourId;
        this.tourName = tourName;
        this.startDatetime = startDatetime;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.assignedGuideId = assignedGuideId;
        this.assignedGuideName = assignedGuideName;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTourId() {
        return tourId;
    }

    public void setTourId(UUID tourId) {
        this.tourId = tourId;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
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

    public UUID getAssignedGuideId() {
        return assignedGuideId;
    }

    public void setAssignedGuideId(UUID assignedGuideId) {
        this.assignedGuideId = assignedGuideId;
    }

    public String getAssignedGuideName() {
        return assignedGuideName;
    }

    public void setAssignedGuideName(String assignedGuideName) {
        this.assignedGuideName = assignedGuideName;
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
        TourScheduleRes that = (TourScheduleRes) o;
        return Objects.equals(id, that.id) && Objects.equals(tourId, that.tourId) && Objects.equals(tourName, that.tourName) && Objects.equals(startDatetime, that.startDatetime) && Objects.equals(maxParticipants, that.maxParticipants) && Objects.equals(status, that.status) && Objects.equals(assignedGuideId, that.assignedGuideId) && Objects.equals(assignedGuideName, that.assignedGuideName) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tourId, tourName, startDatetime, maxParticipants, status, assignedGuideId, assignedGuideName, createdAt);
    }

    @Override
    public String toString() {
        return "TourScheduleRes{" +
                "id=" + id +
                ", tourId=" + tourId +
                ", tourName='" + tourName + ''' +
                ", startDatetime=" + startDatetime +
                ", maxParticipants=" + maxParticipants +
                ", status='" + status + ''' +
                ", assignedGuideId=" + assignedGuideId +
                ", assignedGuideName='" + assignedGuideName + ''' +
                ", createdAt=" + createdAt +
                '}';
    }
}
