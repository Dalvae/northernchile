package com.northernchile.api.tour.dto;

import com.northernchile.api.util.DateTimeUtils;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

public class TourScheduleCreateReq {
    @NotNull
    private UUID tourId;

    // Accept either startDatetime (legacy Instant) OR date + time (preferred)
    private Instant startDatetime;
    
    // Preferred: separate date and time fields
    private LocalDate date;
    private LocalTime time;

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

    /**
     * Get the start datetime as Instant.
     * If date + time are provided, they take precedence and are converted to Instant.
     * Otherwise, the legacy startDatetime field is used.
     */
    public Instant getStartDatetime() {
        // Prefer date + time if both are provided
        if (date != null && time != null) {
            return DateTimeUtils.toInstant(date, time);
        }
        return startDatetime;
    }

    public void setStartDatetime(Instant startDatetime) {
        this.startDatetime = startDatetime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
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
        return Objects.equals(tourId, that.tourId) && 
               Objects.equals(getStartDatetime(), that.getStartDatetime()) && 
               Objects.equals(maxParticipants, that.maxParticipants) && 
               Objects.equals(assignedGuideId, that.assignedGuideId) && 
               Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tourId, getStartDatetime(), maxParticipants, assignedGuideId, status);
    }

    @Override
    public String toString() {
        return "TourScheduleCreateReq{" +
                "tourId=" + tourId +
                ", startDatetime=" + getStartDatetime() +
                ", date=" + date +
                ", time=" + time +
                ", maxParticipants=" + maxParticipants +
                ", assignedGuideId=" + assignedGuideId +
                ", status='" + status + '\'' +
                '}';
    }
}
