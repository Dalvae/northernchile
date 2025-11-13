
package com.northernchile.api.tour.dto;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TourScheduleRes {
    private UUID id;
    private UUID tourId;
    private String tourName;
    private Map<String, String> tourNameTranslations;  // Para el calendario
    private Integer tourDurationHours;  // Para calcular el end time en el calendario
    private Instant startDatetime;
    private Integer maxParticipants;
    private Integer bookedParticipants;  // Participantes ya reservados
    private Integer availableSpots;  // Cupos disponibles (maxParticipants - bookedParticipants)
    private String status;
    private UUID assignedGuideId;
    private String assignedGuideName;
    private Instant createdAt;

    public TourScheduleRes() {
    }

    public TourScheduleRes(UUID id, UUID tourId, String tourName, Map<String, String> tourNameTranslations,
                           Integer tourDurationHours, Instant startDatetime, Integer maxParticipants,
                           Integer bookedParticipants, Integer availableSpots, String status,
                           UUID assignedGuideId, String assignedGuideName, Instant createdAt) {
        this.id = id;
        this.tourId = tourId;
        this.tourName = tourName;
        this.tourNameTranslations = tourNameTranslations;
        this.tourDurationHours = tourDurationHours;
        this.startDatetime = startDatetime;
        this.maxParticipants = maxParticipants;
        this.bookedParticipants = bookedParticipants;
        this.availableSpots = availableSpots;
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

    public Map<String, String> getTourNameTranslations() {
        return tourNameTranslations;
    }

    public void setTourNameTranslations(Map<String, String> tourNameTranslations) {
        this.tourNameTranslations = tourNameTranslations;
    }

    public Integer getTourDurationHours() {
        return tourDurationHours;
    }

    public void setTourDurationHours(Integer tourDurationHours) {
        this.tourDurationHours = tourDurationHours;
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

    public Integer getBookedParticipants() {
        return bookedParticipants;
    }

    public void setBookedParticipants(Integer bookedParticipants) {
        this.bookedParticipants = bookedParticipants;
    }

    public Integer getAvailableSpots() {
        return availableSpots;
    }

    public void setAvailableSpots(Integer availableSpots) {
        this.availableSpots = availableSpots;
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
        return Objects.equals(id, that.id) &&
                Objects.equals(tourId, that.tourId) &&
                Objects.equals(tourName, that.tourName) &&
                Objects.equals(tourNameTranslations, that.tourNameTranslations) &&
                Objects.equals(tourDurationHours, that.tourDurationHours) &&
                Objects.equals(startDatetime, that.startDatetime) &&
                Objects.equals(maxParticipants, that.maxParticipants) &&
                Objects.equals(bookedParticipants, that.bookedParticipants) &&
                Objects.equals(availableSpots, that.availableSpots) &&
                Objects.equals(status, that.status) &&
                Objects.equals(assignedGuideId, that.assignedGuideId) &&
                Objects.equals(assignedGuideName, that.assignedGuideName) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tourId, tourName, tourNameTranslations, tourDurationHours,
                startDatetime, maxParticipants, bookedParticipants, availableSpots,
                status, assignedGuideId, assignedGuideName, createdAt);
    }

    @Override
    public String toString() {
        return "TourScheduleRes{" +
                "id=" + id +
                ", tourId=" + tourId +
                ", tourName='" + tourName + '\'' +
                ", tourNameTranslations=" + tourNameTranslations +
                ", tourDurationHours=" + tourDurationHours +
                ", startDatetime=" + startDatetime +
                ", maxParticipants=" + maxParticipants +
                ", bookedParticipants=" + bookedParticipants +
                ", availableSpots=" + availableSpots +
                ", status='" + status + '\'' +
                ", assignedGuideId=" + assignedGuideId +
                ", assignedGuideName='" + assignedGuideName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
