
package com.northernchile.api.tour.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TourScheduleCreateReq {
    private UUID tourId;
    private Instant startDatetime;
    private Integer maxParticipants;
    private UUID assignedGuideId;

    public TourScheduleCreateReq() {
    }

    public TourScheduleCreateReq(UUID tourId, Instant startDatetime, Integer maxParticipants, UUID assignedGuideId) {
        this.tourId = tourId;
        this.startDatetime = startDatetime;
        this.maxParticipants = maxParticipants;
        this.assignedGuideId = assignedGuideId;
    }

    public UUID getTourId() {
        return tourId;
    }

    public void setTourId(UUID tourId) {
        this.tourId = tourId;
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

    public UUID getAssignedGuideId() {
        return assignedGuideId;
    }

    public void setAssignedGuideId(UUID assignedGuideId) {
        this.assignedGuideId = assignedGuideId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TourScheduleCreateReq that = (TourScheduleCreateReq) o;
        return Objects.equals(tourId, that.tourId) && Objects.equals(startDatetime, that.startDatetime) && Objects.equals(maxParticipants, that.maxParticipants) && Objects.equals(assignedGuideId, that.assignedGuideId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tourId, startDatetime, maxParticipants, assignedGuideId);
    }

    @Override
    public String toString() {
        return "TourScheduleCreateReq{" +
                "tourId=" + tourId +
                ", startDatetime=" + startDatetime +
                ", maxParticipants=" + maxParticipants +
                ", assignedGuideId=" + assignedGuideId +
                '}';
    }
}
