
package com.northernchile.api.tour.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TourRes {
    private UUID id;
    private String name;
    private String description;
    private String category;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;
    private Integer defaultMaxParticipants;
    private Integer durationHours;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public TourRes() {
    }

    public TourRes(UUID id, String name, String description, String category, BigDecimal priceAdult, BigDecimal priceChild, Integer defaultMaxParticipants, Integer durationHours, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.priceAdult = priceAdult;
        this.priceChild = priceChild;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.durationHours = durationHours;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPriceAdult() {
        return priceAdult;
    }

    public void setPriceAdult(BigDecimal priceAdult) {
        this.priceAdult = priceAdult;
    }

    public BigDecimal getPriceChild() {
        return priceChild;
    }

    public void setPriceChild(BigDecimal priceChild) {
        this.priceChild = priceChild;
    }

    public Integer getDefaultMaxParticipants() {
        return defaultMaxParticipants;
    }

    public void setDefaultMaxParticipants(Integer defaultMaxParticipants) {
        this.defaultMaxParticipants = defaultMaxParticipants;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TourRes tourRes = (TourRes) o;
        return Objects.equals(id, tourRes.id) && Objects.equals(name, tourRes.name) && Objects.equals(description, tourRes.description) && Objects.equals(category, tourRes.category) && Objects.equals(priceAdult, tourRes.priceAdult) && Objects.equals(priceChild, tourRes.priceChild) && Objects.equals(defaultMaxParticipants, tourRes.defaultMaxParticipants) && Objects.equals(durationHours, tourRes.durationHours) && Objects.equals(status, tourRes.status) && Objects.equals(createdAt, tourRes.createdAt) && Objects.equals(updatedAt, tourRes.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, category, priceAdult, priceChild, defaultMaxParticipants, durationHours, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "TourRes{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", priceAdult=" + priceAdult +
                ", priceChild=" + priceChild +
                ", defaultMaxParticipants=" + defaultMaxParticipants +
                ", durationHours=" + durationHours +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
