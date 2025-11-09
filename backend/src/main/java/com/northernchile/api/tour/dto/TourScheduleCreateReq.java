
package com.northernchile.api.tour.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TourScheduleCreateReq {
    @NotNull
    private UUID tourId;

    @NotNull
    private Instant startDatetime;

    @NotNull
    @Min(1)
    private Integer maxParticipants;
    private UUID assignedGuideId;
    private String status;

    public TourScheduleCreateReq() {
    }

    public TourScheduleCreateReq(UUID tourId, Instant startDatetime, Integer maxParticipants, UUID assignedGuideId) {
        this.tourId = tourId;
        this.startDatetime = startDatetime;
        this.maxParticipants = maxParticipants;
        this.assignedGuideId = assignedGuideId;
    }

    public TourScheduleCreateReq(UUID tourId, Instant startDatetime, Integer maxParticipants, UUID assignedGuideId, String status) {
        this.tourId = tourId;
        this.startDatetime = startDatetime;
        this.maxParticipants = maxParticipants;
        this.assignedGuideId = assignedGuideId;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TourScheduleCreateReq that = (TourScheduleCreateReq) o;
        return Objects.equals(tourId, that.tourId) && Objects.equals(startDatetime, that.startDatetime) && Objects.equals(maxParticipants, that.maxParticipants) && Objects.equals(assignedGuideId, that.assignedGuideId) && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tourId, startDatetime, maxParticipants, assignedGuideId, status);
    }

    @Override
    public String toString() {
        return "TourScheduleCreateReq{" +
                "tourId=" + tourId +
                ", startDatetime=" + startDatetime +
                ", maxParticipants=" + maxParticipants +
                ", assignedGuideId=" + assignedGuideId +
                ", status='" + status + '\'' +
                '}';
    }
}
