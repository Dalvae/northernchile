
package com.northernchile.api.tour.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.northernchile.api.model.TourImage; // Nueva importación
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List; // Asegurarse de que esté importado
import java.util.Map; // Asegurarse de que esté importado
import java.util.Objects;
import java.util.UUID;

public class TourRes {
    private UUID id;
    private Map<String, String> nameTranslations;
    private Map<String, String> descriptionTranslations;
    private String category;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;
    private Integer defaultMaxParticipants;
    private Integer durationHours;
    private String status;
    private List<TourImageRes> images; // CAMBIO: List<TourImageRes>
    @JsonProperty("isMoonSensitive")
    private boolean isMoonSensitive;
    @JsonProperty("isWindSensitive")
    private boolean isWindSensitive;
    @JsonProperty("isCloudSensitive")
    private boolean isCloudSensitive; // NUEVO
    private Instant createdAt;
    private Instant updatedAt;

    public TourRes() {
    }

    public TourRes(UUID id, Map<String, String> nameTranslations, Map<String, String> descriptionTranslations, String category, BigDecimal priceAdult, BigDecimal priceChild, Integer defaultMaxParticipants, Integer durationHours, String status, List<TourImageRes> images, boolean isMoonSensitive, boolean isWindSensitive, boolean isCloudSensitive, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.nameTranslations = nameTranslations;
        this.descriptionTranslations = descriptionTranslations;
        this.category = category;
        this.priceAdult = priceAdult;
        this.priceChild = priceChild;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.durationHours = durationHours;
        this.status = status;
        this.images = images;
        this.isMoonSensitive = isMoonSensitive;
        this.isWindSensitive = isWindSensitive;
        this.isCloudSensitive = isCloudSensitive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Map<String, String> getNameTranslations() {
        return nameTranslations;
    }

    public void setNameTranslations(Map<String, String> nameTranslations) {
        this.nameTranslations = nameTranslations;
    }

    public Map<String, String> getDescriptionTranslations() {
        return descriptionTranslations;
    }

    public void setDescriptionTranslations(Map<String, String> descriptionTranslations) {
        this.descriptionTranslations = descriptionTranslations;
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

    public List<TourImageRes> getImages() {
        return images;
    }

    public void setImages(List<TourImageRes> images) {
        this.images = images;
    }

    public boolean isMoonSensitive() {
        return isMoonSensitive;
    }

    public void setMoonSensitive(boolean moonSensitive) {
        isMoonSensitive = moonSensitive;
    }

    public boolean isWindSensitive() {
        return isWindSensitive;
    }

    public void setWindSensitive(boolean windSensitive) {
        isWindSensitive = windSensitive;
    }

    public boolean isCloudSensitive() {
        return isCloudSensitive;
    }

    public void setCloudSensitive(boolean cloudSensitive) {
        isCloudSensitive = cloudSensitive;
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
        return isMoonSensitive == tourRes.isMoonSensitive && isWindSensitive == tourRes.isWindSensitive && isCloudSensitive == tourRes.isCloudSensitive && Objects.equals(id, tourRes.id) && Objects.equals(nameTranslations, tourRes.nameTranslations) && Objects.equals(descriptionTranslations, tourRes.descriptionTranslations) && Objects.equals(category, tourRes.category) && Objects.equals(priceAdult, tourRes.priceAdult) && Objects.equals(priceChild, tourRes.priceChild) && Objects.equals(defaultMaxParticipants, tourRes.defaultMaxParticipants) && Objects.equals(durationHours, tourRes.durationHours) && Objects.equals(status, tourRes.status) && Objects.equals(images, tourRes.images) && Objects.equals(createdAt, tourRes.createdAt) && Objects.equals(updatedAt, tourRes.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nameTranslations, descriptionTranslations, category, priceAdult, priceChild, defaultMaxParticipants, durationHours, status, images, isMoonSensitive, isWindSensitive, isCloudSensitive, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "TourRes{" +
                "id=" + id +
                ", nameTranslations=" + nameTranslations +
                ", descriptionTranslations=" + descriptionTranslations +
                ", category='" + category + '\'' +
                ", priceAdult=" + priceAdult +
                ", priceChild=" + priceChild +
                ", defaultMaxParticipants=" + defaultMaxParticipants +
                ", durationHours=" + durationHours +
                ", status='" + status + '\'' +
                ", images=" + images +
                ", isMoonSensitive=" + isMoonSensitive +
                ", isWindSensitive=" + isWindSensitive +
                ", isCloudSensitive=" + isCloudSensitive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
