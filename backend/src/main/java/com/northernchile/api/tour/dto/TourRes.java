
package com.northernchile.api.tour.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import java.util.Objects;
import java.util.UUID;

public class TourRes {
    private UUID id;
    private String slug;
    private Map<String, String> nameTranslations;
    private String category;
    private BigDecimal price;
    private Integer defaultMaxParticipants;
    private Integer durationHours;
    private LocalTime defaultStartTime;
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
    private String contentKey;

    // Structured content (already translated to requested language)
    private String guideName;
    private List<ItineraryItem> itinerary;
    private List<String> equipment;
    private List<String> additionalInfo;

    private Map<String, java.util.List<ContentBlock>> descriptionBlocksTranslations;
 
    public TourRes() {

    }

    public TourRes(UUID id, String slug, Map<String, String> nameTranslations, String category, BigDecimal price, Integer defaultMaxParticipants, Integer durationHours, LocalTime defaultStartTime, String status, List<TourImageRes> images, boolean isMoonSensitive, boolean isWindSensitive, boolean isCloudSensitive, Instant createdAt, Instant updatedAt, String contentKey, String guideName, List<ItineraryItem> itinerary, List<String> equipment, List<String> additionalInfo, Map<String, java.util.List<ContentBlock>> descriptionBlocksTranslations) {
        this.id = id;
        this.slug = slug;
        this.nameTranslations = nameTranslations;
        this.category = category;
        this.price = price;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.durationHours = durationHours;
        this.defaultStartTime = defaultStartTime;
        this.status = status;
        this.images = images;
        this.isMoonSensitive = isMoonSensitive;
        this.isWindSensitive = isWindSensitive;
        this.isCloudSensitive = isCloudSensitive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.contentKey = contentKey;
        this.guideName = guideName;
        this.itinerary = itinerary;
        this.equipment = equipment;
        this.additionalInfo = additionalInfo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Map<String, String> getNameTranslations() {
        return nameTranslations;
    }

    public void setNameTranslations(Map<String, String> nameTranslations) {
        this.nameTranslations = nameTranslations;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public LocalTime getDefaultStartTime() {
        return defaultStartTime;
    }

    public void setDefaultStartTime(LocalTime defaultStartTime) {
        this.defaultStartTime = defaultStartTime;
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

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    public List<ItineraryItem> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<ItineraryItem> itinerary) {
        this.itinerary = itinerary;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
    }

    public List<String> getAdditionalInfo() {
        return additionalInfo;
    }
 
    public void setAdditionalInfo(List<String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Map<String, java.util.List<ContentBlock>> getDescriptionBlocksTranslations() {
        return descriptionBlocksTranslations;
    }

    public void setDescriptionBlocksTranslations(Map<String, java.util.List<ContentBlock>> descriptionBlocksTranslations) {
        this.descriptionBlocksTranslations = descriptionBlocksTranslations;
    }
 
    @Override

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TourRes tourRes = (TourRes) o;
        return isMoonSensitive == tourRes.isMoonSensitive && isWindSensitive == tourRes.isWindSensitive && isCloudSensitive == tourRes.isCloudSensitive && Objects.equals(id, tourRes.id) && Objects.equals(slug, tourRes.slug) && Objects.equals(nameTranslations, tourRes.nameTranslations) && Objects.equals(category, tourRes.category) && Objects.equals(price, tourRes.price) && Objects.equals(defaultMaxParticipants, tourRes.defaultMaxParticipants) && Objects.equals(durationHours, tourRes.durationHours) && Objects.equals(status, tourRes.status) && Objects.equals(images, tourRes.images) && Objects.equals(createdAt, tourRes.createdAt) && Objects.equals(updatedAt, tourRes.updatedAt) && Objects.equals(contentKey, tourRes.contentKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slug, nameTranslations, category, price, defaultMaxParticipants, durationHours, status, images, isMoonSensitive, isWindSensitive, isCloudSensitive, createdAt, updatedAt, contentKey);
    }

    @Override
    public String toString() {
        return "TourRes{" +
                "id=" + id +
                ", slug='" + slug + '\'' +
                ", nameTranslations=" + nameTranslations +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", defaultMaxParticipants=" + defaultMaxParticipants +
                ", durationHours=" + durationHours +
                ", status='" + status + '\'' +
                ", images=" + images +
                ", isMoonSensitive=" + isMoonSensitive +
                ", isWindSensitive=" + isWindSensitive +
                ", isCloudSensitive=" + isCloudSensitive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", contentKey='" + contentKey + '\'' +
                '}';
    }
}
